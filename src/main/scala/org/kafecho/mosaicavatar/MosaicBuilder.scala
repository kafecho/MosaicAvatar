package org.kafecho.mosaicavatar

import javax.imageio._
import java.awt.image._
import java.net.URL
import java.awt.Color
import org.apache.http.client.methods.HttpGet
import scala.xml.{XML, Node}
import org.apache.commons.logging.{LogFactory, Log}

import org.apache.http.impl.client.DefaultHttpClient
import java.text.SimpleDateFormat
import org.apache.http.{HttpEntity,HttpStatus};
import java.util.Date

import TimeUtil._

/**
 * TODO
 * -DONE: Add utility class to measure and log the execution times of methods.
 * -DONE: Change the way data such as buffered image and color are referenced to enable better garbage collection.
 * -Handle PNG images with transparent background.
 */

// Parse time in RFC3339 format. 
object RFC3339 extends SimpleDateFormat("yyyy-MM-dd'T'h:m:ss")

// Wrapper around a Color object to provide additional features using Scala's implicit conversions.
class RichColor(val me : Color){

  // Compute the distance between 2 colors.
  def distance (other : Color) : Double = Math.sqrt( Math.pow(me.getRed-other.getRed,2) + Math.pow(me.getGreen-other.getGreen,2) + Math.pow(me.getBlue-other.getBlue,2))

  // Outputs the color as a css string.
  def css = "background-color: rgb(" + me.getRed + "," + me.getGreen + "," + me.getBlue + ");"
}

object ImplicitConversions{
  implicit def richColor ( c : Color ) = new RichColor (c)
}

import ImplicitConversions._

class UserImage (val info : UserInfo, val buffer : BufferedImage){
  lazy val averageColor = ImageUtil.getAverageColor(buffer)
  lazy val width = buffer.getWidth
  lazy val height = buffer.getHeight
  def distance ( other : UserImage) =  averageColor.distance(other.averageColor )
}

class MosaicBuilder {

  val client = new TwitterAPIClient

  /**
   * Build a mosaic for a given Twitter user.
   * Possible failures:
   * -Twitter cannot be reached.
   * -The user does not exist.
   * -The user does not have any friends.
   * -There are not enough API calls left.
   * -The client has exceeded the Twitter API rate limit.
   * -The profile picture of the user has an unrecognized format.
   */

  def buildMosaic(screenName : String) : Mosaic = {
    timeThis(log,"Generating mosaic for user " + screenName){
      val RateLimitStatus(_,remaining,_) = client.getRateLimitStatus
      if (remaining < 2) throw new InsufficientRemainingCallsException

      val userInfo : UserInfo = client.fetchUserInfo(screenName)

      if (userInfo.accountProtected) throw new AccountProtectedException(screenName)

      if (userInfo.friendsCount == 0) throw new NoFriendsException(screenName)

      ImageUtil.buildUserImage(userInfo) match{

        case None => throw new InvalidPictureFormat(screenName,userInfo.imageURL)

        case Some(userThumbnail) => {

          val friendsProfiles = client.fetchFriendsInfo(screenName)

          if (friendsProfiles.isEmpty) throw new NoFriendsException(screenName)

          val counts = scala.collection.mutable.Map[UserInfo,Int]()

          var friendsThumbnails : List[UserImage]=List()
          friendsProfiles.foreach{
            fp =>
              ImageUtil.buildUserImage(fp) match{
                case Some(image) => friendsThumbnails = image :: friendsThumbnails
                case _ =>
              }
          }
          val backgroundTiles : Array[Array[Color]]     = new  Array(userThumbnail.height,userThumbnail.width)
          val foregroundTiles : Array[Array[UserInfo]]  = new Array(userThumbnail.height,userThumbnail.width)

          for (
            y <-0 until userThumbnail.height;
            x <- 0 until userThumbnail.width ;
            color  = new Color( userThumbnail.buffer.getRGB(x,y))
          ){
            backgroundTiles(y)(x) = color
          }

          for (
            y <-0 until userThumbnail.height;
            x <-0 until userThumbnail.width;
            color = new Color (userThumbnail.buffer.getRGB(x,y));
            thumbnail = friendsThumbnails.reduceLeft( (a,b) => if ( a.averageColor.distance(color) < b.averageColor.distance(color)) a else b)
          ){
            val mt  = thumbnail.info
            foregroundTiles(y)(x) = mt
            val oldCount = if (counts.contains(mt)) counts(mt) else 0
            counts += (mt -> (oldCount + 1))
          }

          val ranked = counts.toList.sort( (elt1,elt2) => elt1._2 > elt2._2 )

          val mostUsed    = ranked.take(3)
          val leastUsed   = ranked.takeRight(3)

          log.info("Most used friends: "  + mostUsed)
          log.info("Least used friends: " + leastUsed)

          Mosaic(
            userThumbnail.info.name,
            userThumbnail.info.screenName,
            userThumbnail.width,
            userThumbnail.height,
            mostUsed,
            leastUsed,
            backgroundTiles,
            foregroundTiles)
        }
      }
    }
  }
}

object Test{
  def main (args : Array[String]){

    val log : Log = LogFactory.getLog(this.getClass)

    val builder : MosaicBuilder = new MosaicBuilder

    builder.buildMosaic("metysj")

//    val friends : List[UserInfo] = builder.client.fetchFriendsInfo("gbelrose")
//
//    friends.foreach{ friend =>
//      try{
//        builder.buildMosaic(friend.screenName).save
//      }catch{
//        case e : Exception => log.error(e,e)
//      }
//    }
  }
}

