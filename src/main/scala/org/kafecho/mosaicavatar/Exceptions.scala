package org.kafecho.mosaicavatar


/**
 * User: guillaume
 * Date: Mar 16, 2010
 * Time: 7:24:24 PM
 */

class InvalidPictureFormat(screenName : String, url : String) extends Exception ("The profile picture of " + screenName + " @" + url + " uses an unsupported picture format.")

class NoFriendsException(screenName : String) extends Exception(screenName + " is not following anyone.")
class AccountProtectedException (screenName : String) extends Exception (screenName + " has a protected account.")

class TwitterAPIException extends Exception

class UnauthorizedException extends TwitterAPIException
class UserNotFoundException extends TwitterAPIException
class RateLimitedException  extends TwitterAPIException
class InternalServerException extends TwitterAPIException
class ServiceMaintenanceException extends TwitterAPIException
class ServiceOverloadedException extends TwitterAPIException

class InsufficientRemainingCallsException extends Exception("Insufficient API calls remaining.")

/**
* 400 Bad Request: The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.
* 401 Unauthorized: Authentication credentials were missing or incorrect.
* 403 Forbidden: The request is understood, but it has been refused.  An accompanying error message will explain why. This code is used when requests are being denied due to update limits.
* 404 Not Found: The URI requested is invalid or the resource requested, such as a user, does not exists.
* //406 Not Acceptable: Returned by the Search API when an invalid format is specified in the request.
* 420 Enhance Your Calm: Returned by the Search and Trends API  when you are being rate limited.
* 500 Internal Server Error: Something is broken.  Please post to the group so the Twitter team can investigate.
* 502 Bad Gateway: Twitter is down or being upgraded.
* 503 Service Unavailable: The Twitter servers are up, but overloaded with requests. Try again later.
**/

object ExceptionFactory{
  def build (statusCode : Int) = {
    statusCode match{
      case 401 => new UnauthorizedException
      case 404 => new UserNotFoundException
      case 420 => new RateLimitedException
      case 500 => new InternalServerException
      case 502 => new ServiceMaintenanceException
      case 503 => new ServiceOverloadedException
      case _ => new TwitterAPIException
    }
  }
}