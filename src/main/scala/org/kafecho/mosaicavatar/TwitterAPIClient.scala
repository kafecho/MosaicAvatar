package org.kafecho.mosaicavatar

import java.util.Date
import org.apache.http.{HttpStatus, HttpEntity}
import org.apache.http.client.methods.HttpGet
import org.apache.commons.logging.{LogFactory, Log}
import org.apache.http.impl.client.DefaultHttpClient
import scala.xml.{Node, XML}

/**
 * Created by IntelliJ IDEA.
 * User: guillaume
 * Date: Mar 26, 2010
 * Time: 9:37:36 AM
 * To change this template use File | Settings | File Templates.
 */

// Holds information about a Twitter user.
case class UserInfo(
        name : String,
        screenName : String,
        imageURL : String,
        accountProtected : Boolean,
        friendsCount     : Int
        )

// Information about the API rate status.
case class RateLimitStatus(hourlyLimit : Int, remainingHits : Int, resetTime : Date)

class TwitterAPIClient{

  private val httpClient = new DefaultHttpClient

  private val log : Log = LogFactory.getLog(getClass)

  // Generic method that GETs the content of a URL and passes it to a handler function which processes the response content to produce some data.
  // The GET method takes care of releasing resources when processing is done.
  // It also throws appropriate exceptions when certain conditions occur.
  // It eventually returns the data structure created by the handler function.
  def GET[T]( url : String)(handler : HttpEntity => T ) : T = {
    val get = new HttpGet(url)
    val response = httpClient.execute(get)
    response.getStatusLine.getStatusCode match{
      case HttpStatus.SC_OK =>
        val entity = response.getEntity
        val output : T = handler (entity)
        entity.consumeContent
        return output
      case error =>
        if (log.isErrorEnabled) log.error("The request " + get.getURI + " resulted in an HTTP error " + response.getStatusLine.toString)
        get.abort
        throw ExceptionFactory.build(error)
    }
  }

  def getRateLimitStatus : RateLimitStatus = GET("http://api.twitter.com/1/account/rate_limit_status.xml"){
    entity =>
      val xml = XML.load (entity.getContent)
      val remainingHits : Int  = (xml \ "remaining-hits" text).toInt
      val hourlyLimit   : Int  = (xml \ "hourly-limit" text).toInt
      val resetTime     : Date = RFC3339.parse( xml \ "reset-time" text)
      return RateLimitStatus(hourlyLimit,remainingHits,resetTime)
  }


  def buildUserInfo ( xml : Node) : UserInfo = {
    val name = xml \ "name" text
    val screenName = xml \ "screen_name" text
    val profileImageURL = (xml \ "profile_image_url" text).toString
    val accountProtected : Boolean = (xml \ "protected" text).toBoolean
    val friendsCount = (xml \ "friends_count" text).toInt
    return UserInfo(name, screenName, profileImageURL,accountProtected, friendsCount)
  }

  def fetchUserInfo(screenName : String) : UserInfo = GET("http://api.twitter.com/1/users/show.xml?screen_name=" + screenName){
    entity =>
      return buildUserInfo(XML.load (entity.getContent))
  }

  def fetchFriendsInfo(screenName : String) : List[UserInfo]= GET ("http://api.twitter.com/1/statuses/friends.xml?screen_name=" + screenName){
    entity =>
      val xml = XML.load (entity.getContent)
      return (xml \ "user").toList.map ( user => buildUserInfo(user))
  }

}