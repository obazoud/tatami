package fr.ippon.tatami.repository.cassandra;

import static fr.ippon.tatami.config.ColumnFamilyKeys.TAGLINE_CF;
import static me.prettyprint.hector.api.factory.HFactory.createSliceQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import me.prettyprint.cassandra.model.CqlQuery;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hom.EntityManagerImpl;

import org.springframework.stereotype.Repository;

import fr.ippon.tatami.domain.TagLineCount;
import fr.ippon.tatami.repository.TagLineRepository;
import fr.ippon.tatami.service.util.TatamiConstants;

@Repository
public class CassandraTagLineRepository extends CassandraAbstractRepository implements TagLineRepository
{
	@Inject
	private EntityManagerImpl em;

	@Inject
	private Keyspace keyspaceOperator;

	@Override
	public void addTweet(String tag, String tweetId)
	{

		TagLineCount tagLineCount = em.find(TagLineCount.class, tag);

		if (tagLineCount == null)
		{
			tagLineCount = new TagLineCount();
			tagLineCount.setTag(tag);
		}

		CqlQuery<String, Long, String> cqlQuery = new CqlQuery<String, Long, String>(keyspaceOperator, se, le, se);
		cqlQuery.setQuery("INSERT INTO TagLine(KEY,'" + tagLineCount.getTweetCount() + "') VALUES('" + tag + "','" + tweetId + "')");
		cqlQuery.execute();

		tagLineCount.incrementTweetCount();

		em.persist(tagLineCount);
	}

	@Override
	public Collection<String> findTweetsForTag(String tag)
	{
		Collection<String> tweetIds = null;
		TagLineCount tagLineCount = em.find(TagLineCount.class, tag);
		if (tagLineCount == null)
		{
			// TODO Functional exception
			tweetIds = Arrays.asList();
		}
		else
		{
			long endTweetColumn = tagLineCount.getTweetCount() - 1;
			long startTweetColumn = tagLineCount.getTweetCount() - TatamiConstants.DEFAULT_TAG_LIST_SIZE - 1;

			List<HColumn<Long, String>> columns = createSliceQuery(keyspaceOperator, se, le, se).setColumnFamily(TAGLINE_CF).setKey("tag")
					.setRange(endTweetColumn, startTweetColumn, true, TatamiConstants.DEFAULT_TAG_LIST_SIZE).execute().get().getColumns();

			tweetIds = new ArrayList<String>();
			for (HColumn<Long, String> column : columns)
			{
				tweetIds.add(column.getValue());
			}
		}
		return tweetIds;
	}

	@Override
	public Collection<String> findTweetsRangeForTag(String tag, int start, int end)
	{
		Collection<String> tweetIds = null;
		TagLineCount tagLineCount = em.find(TagLineCount.class, tag);
		if (tagLineCount == null)
		{
			// TODO Functional exception
			tweetIds = Arrays.asList();
		}
		else
		{

			long maxTweetColumn = tagLineCount.getTweetCount() - 1;
			long endTweetColumn = maxTweetColumn - start + 1;
			long startTweetColumn = maxTweetColumn - end + 1;

			if (startTweetColumn < 0)
			{
				startTweetColumn = 0;
			}

			if (endTweetColumn <= 0)
			{
				endTweetColumn = 0;
			}

			List<HColumn<Long, String>> columns = createSliceQuery(keyspaceOperator, se, le, se).setColumnFamily(TAGLINE_CF).setKey(tag)
					.setRange(endTweetColumn, startTweetColumn, true, end - start + 1).execute().get().getColumns();

			tweetIds = new ArrayList<String>();

			for (HColumn<Long, String> column : columns)
			{
				tweetIds.add(column.getValue());
			}
		}
		return tweetIds;
	}
}
