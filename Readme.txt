MAvatar (or MosaicAvatar) is a Scala pet project that I have been working on lately based around Twitter.
The idea is quite simple: MAvatar creates a mosaic of a user's profile picture.
The mosaic itself is composed of profile images chosen among the user's friends.
Essentially, for each pixel in the profile picture, the algorithm picks the friend's profile with the closest average color.
The end result is formatted as a web page which provides a zoom and displays some useful (if not hurtful) statistics to tell you which friends were the most or least used in the process of creating the mosaic.
The process is entirely automated and works for any Twitter user as long as (1) her/his account is not protected and (2) she/he is actually following other users.
MAvatar is mainly built using Scala (whose XML support really wins when it comes to parsing the Twitter API's XML and to generate HTML) and features some simple jQuery and jQuery UI.