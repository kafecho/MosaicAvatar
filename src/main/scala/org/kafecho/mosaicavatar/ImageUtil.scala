package org.kafecho.mosaicavatar

import org.apache.commons.logging.{LogFactory, Log}
import java.net.URL
import javax.imageio.ImageIO
import java.awt.Color
import java.awt.image.BufferedImage

object ImageUtil{
  private val log : Log = LogFactory.getLog(getClass)

  def getAverageColor(buffer : BufferedImage) : Color = {
    var avgRed = 0
    var avgGreen = 0
    var avgBlue = 0
    val (width,height) = (buffer.getWidth, buffer.getHeight)
    for (x <-0 until width; y <-0 until height; color = new Color(buffer.getRGB(x,y))){
      avgRed 		+= color.getRed
      avgGreen 	+= color.getGreen
      avgBlue 	+= color.getBlue
    }
    val n = width * height
    return new Color(avgRed/n,avgGreen/n, avgBlue/n)
  }

  def buildUserImage(info : UserInfo) : Option[UserImage] = {
    try{
      Some(new UserImage(info,ImageIO.read( new URL(info.imageURL))))
    }
    catch{
      case ex : Exception =>
        if (log.isErrorEnabled) log.error("An exception occured while creating the buffered image for " + info.imageURL, ex)
        None
    }
  }
}