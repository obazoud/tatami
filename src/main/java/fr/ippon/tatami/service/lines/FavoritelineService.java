package fr.ippon.tatami.service.lines;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ippon.tatami.domain.Tweet;
import fr.ippon.tatami.domain.User;
import fr.ippon.tatami.exception.FunctionalException;
import fr.ippon.tatami.repository.FavoriteIndexRepository;
import fr.ippon.tatami.service.pipeline.tweet.FavoriteHandler;
import fr.ippon.tatami.service.pipeline.tweet.TweetHandler;
import fr.ippon.tatami.service.util.TatamiConstants;

public class FavoritelineService extends AbstractlineService implements FavoriteHandler, TweetHandler
{

	private final Logger log = LoggerFactory.getLogger(FavoritelineService.class);

	private FavoriteIndexRepository favoriteIndexRepository;

	@Override
	public void onAddToFavorite(Tweet tweet) throws FunctionalException
	{
		User currentUser = userService.getCurrentUser();

		log.debug("Adding tweet : {} to favorites for {} ", tweet.getTweetId(), currentUser.getLogin());

		Collection<String> favoriteTweets = this.favoriteLineRepository.findFavoritesForUser(currentUser);

		if (!favoriteTweets.contains(tweet.getTweetId()))
		{
			this.favoriteLineRepository.addFavorite(currentUser, tweet.getTweetId());
			this.favoriteIndexRepository.addTweetToFavoriteIndex(currentUser.getLogin(), tweet.getTweetId());
		}
		else
		{
			throw new FunctionalException("You already have this tweet in your favorites !");
		}

	}

	@Override
	public void onRemoveFromFavorite(Tweet tweet) throws FunctionalException
	{
		User currentUser = userService.getCurrentUser();

		log.debug("Removing tweet : {} from favorites for {} ", tweet.getTweetId(), currentUser.getLogin());

		Collection<String> favoriteTweets = this.favoriteLineRepository.findFavoritesForUser(currentUser);

		if (favoriteTweets.contains(tweet.getTweetId()))
		{
			this.favoriteLineRepository.removeFavorite(currentUser, tweet.getTweetId());
			this.favoriteIndexRepository.removeTweetFromFavoriteIndex(currentUser.getLogin(), tweet.getTweetId());
		}
		else
		{
			throw new FunctionalException("You do not have this tweet in your favorites so you can't remove it!");
		}

	}

	@Override
	public void onTweetPost(Tweet tweet) throws FunctionalException
	{
		// Do nothing

	}

	@Override
	public void onTweetRemove(Tweet tweet) throws FunctionalException
	{
		Collection<String> userLogins = this.favoriteIndexRepository.getUsersForTweetFromIndex(tweet.getTweetId());

		User user;
		for (String login : userLogins)
		{
			user = this.userService.getUserByLogin(login);
			this.favoriteLineRepository.removeFavorite(user, tweet.getTweetId());
		}

		this.favoriteIndexRepository.removeIndexForTweet(tweet.getTweetId());
	}

	public Collection<Tweet> getFavoriteslineRange(String startTweetId, int count) throws FunctionalException
	{
		User currentUser = userService.getCurrentUser();

		if (startTweetId == null && count < TatamiConstants.DEFAULT_FAVORITE_LIST_SIZE)
		{
			count = TatamiConstants.DEFAULT_FAVORITE_LIST_SIZE;
		}

		Collection<String> tweetIds = favoriteLineRepository.findFavoritesRangeForUser(currentUser, startTweetId, count);
		return this.buildTweetsList(currentUser, tweetIds);
	}

	public void setFavoriteIndexRepository(FavoriteIndexRepository favoriteIndexRepository)
	{
		this.favoriteIndexRepository = favoriteIndexRepository;
	}

}
