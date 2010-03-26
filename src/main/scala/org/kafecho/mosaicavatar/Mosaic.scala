package org.kafecho.mosaicavatar

import java.awt.Color
import scala.xml.XML

import ImplicitConversions._

case class Mosaic(
        name : String,
        screenName : String,
        width: Int,
        height : Int,
        mostUsed:List[Tuple2[UserInfo,Int]],
        leastUsed:List[Tuple2[UserInfo,Int]],
        background : Array[Array[Color]],
        foreground : Array[Array[UserInfo]])
{
  private def buildImage (name : String, screenName : String, url : String) = {
    <a title={name} href={"http://www.twitter.com/" + screenName }><img class={"thumbnail tile " + screenName} src={url}/></a>
  }

  private def buildRectangle(color : Color) = {
    <div style={color.css} class= "tile" />
  }

  private def formatTimesUsed(count : Int) : String ={
    count match {
      case 1 => "once"
      case 2 => "twice"
      case c => c + " times"
    }
  }

  private def mosaicLogo = {
    "Mosaic".map( c => <span id={"mosaic"+c.toUpperCase}>{c}</span>)
  }

  private def mosaicTitle =
    <div id="mosaicTitle">
      {
        name + "'" + (if (name.endsWith("s")) " " else "s ")
      }
      {mosaicLogo.toList}
    </div>

  private def statList(id : String, description : String, list : List[Tuple2[UserInfo,Int]])={
    <div id={id}>
      <div class="listTitle">{description}</div>
      <ul>
        {
        list.map{ tuple =>
          val info : UserInfo = tuple._1
          val count: Int      = tuple._2
          <li>
              <a href={"http://www.twitter.com/" + info.screenName}> <img src={info.imageURL}/></a>
              {info.name} <span class="timesUsed">(used {formatTimesUsed(count)})</span>
          </li>
        }
        }
      </ul>
    </div>
  }

  private def toHTML = {
    <html>
      <head>
        <link href="mavatar.css" rel="Stylesheet" type="text/css"></link>
        <link type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.0/themes/ui-lightness/jquery-ui.css" rel="Stylesheet" />
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.0/jquery-ui.min.js"></script>
        <script type="text/javascript" src="mavatar.js"></script>
      </head>

      <body>

        <div id="main">
          {mosaicTitle}
          <div id="leftPanel">

            <div id="zoomPanel">
              <div id="zoom">Zoom</div>
              <div id="slider"></div>
            </div>

            <div id="statistics">
              {statList("mostUsed", "Most used friends",mostUsed)}
              {statList("leastUsed","Least used friends",leastUsed)}
            </div>
          </div>

          <div id="centerPanel">
            <div id="background" class="container">
              <table cellpadding="0" cellspacing="0">
                <tbody>
                  {
                  background.toList.map { row =>
                    <tr>
                      {
                      row.toList.map { color =>
                        <td>{buildRectangle(color)}</td>
                      }
                      }
                    </tr>
                  }
                  }
                </tbody>
              </table>
            </div>

            <div id="overlay" class="container">
              <table cellpadding="0" cellspacing="0">
                <tbody>
                  {
                  foreground.toList.map { row =>
                    <tr>
                      {
                      row.toList.map { tile =>
                        <td>{buildImage(tile.name, tile.screenName, tile.imageURL)}</td>
                      }
                      }
                    </tr>
                  }
                  }
                </tbody>
              </table>
            </div>
          </div>

          <div id="footer">
          </div>

        </div>
      </body>
    </html>
  }

  def save{
    XML.save(screenName +".html",toHTML)
  }
}