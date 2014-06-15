package com.chechev.phd.search;

import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.chechev.phd.recommendedstream.datatypes.ExtendedPost;
import com.chechev.phd.utils.GlobalConstants;
import com.chechev.phd.utils.GlobalConstants;
import com.chechev.phd.utils.GlobalConstants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Milen
 */
public class DataSearcher {

    public static final String[] fields = {};
    private CommonsHttpSolrServer server;
    private Mongo m;

    public List<ExtendedPost> getPosts(String queryText, String userId, int count) {
        List<ExtendedPost> list = new LinkedList<ExtendedPost>();
        CommonsHttpSolrServer server = getSolrServer();
        server.setParser(new XMLResponseParser());
        SolrQuery query = new SolrQuery();

        if (queryText.isEmpty()) {
            queryText = "*:*";
        } else {
            queryText = "(text:" + queryText + " || " + "from:" + queryText + " || " + "comments:" + queryText + ")";
        }

        queryText += " && owner_id:" + userId;
        System.out.println(queryText);
        query.setQuery(queryText);
        query.addSortField("updatedTime", SolrQuery.ORDER.desc);
        query.setRows(2000);

        try {
            QueryResponse rsp = server.query(query);
            SolrDocumentList docs = rsp.getResults();

            for (SolrDocument d : docs) {
                ExtendedPost p = convertToPost(d);
                if (p != null) {
                    list.add(p);
                }
            }
        } catch (SolrServerException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }


        return list;
    }

    public ExtendedPost convertToPost(SolrDocument doc) {
        Mongo m = null;
        try {
            m = getMongo();
            DB db = m.getDB("mydb");
            DBCollection coll = db.getCollection("docs");
            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(doc.getFieldValue("_id").toString()));
            DBObject o = coll.findOne(query);
            if (o == null) {
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            o.removeField("_id");
            o.removeField("$oid");
            o.removeField("owner_id");
            o.put("updatedTime", ((Long) o.get("updatedTime")) / 1000);
            o.put("createdTime", ((Long) o.get("createdTime")) / 1000);

            return mapper.readValue(JSON.serialize(o), ExtendedPost.class);
        } catch (IOException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void deleteAllPosts() {
        try {
            getSolrServer().deleteByQuery("*:*");
            getSolrServer().commit();
        } catch (SolrServerException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public SolrInputDocument convertToSolrDocument(DBObject object) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("_id", object.get("_id"));
        doc.addField("from", object.get("from"));
        doc.addField("owner_id", object.get("owner_id"));
        doc.addField("updatedTime", object.get("updatedTime"));
        StringBuilder text = new StringBuilder();
        if (object.get("message") != null) {
            text.append(" ").append(object.get("message"));
        }
        if (object.get("name") != null) {
            text.append(" ").append(object.get("name"));
        }
        if (object.get("caption") != null) {
            text.append(" ").append(object.get("caption"));
        }
        if (object.get("description") != null) {
            text.append(" ").append(object.get("description"));
        }

        doc.addField("text", text.toString());


        doc.addField("comments", object.get("comments") + "");
        return doc;
    }

    public void addPost(DBObject object) {
        try {
            CommonsHttpSolrServer server = getSolrServer();
            server.add(convertToSolrDocument(object));
        } catch (SolrServerException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void commit() {
        try {
            CommonsHttpSolrServer server = getSolrServer();
            server.commit();
        } catch (SolrServerException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deletePost(String mongodbId) {
        try {
            getSolrServer().deleteByQuery("_id:" + mongodbId);
            getSolrServer().commit();
        } catch (SolrServerException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private CommonsHttpSolrServer getSolrServer() {

        /*
         CommonsHttpSolrServer is thread-safe and if you are using the following constructor,
         you *MUST* re-use the same instance for all requests.  If instances are created on
         the fly, it can cause a connection leak. The recommended practice is to keep a
         static instance of CommonsHttpSolrServer per solr server url and share it for all requests.
         See https://issues.apache.org/jira/browse/SOLR-861 for more details
         */
        if (server == null) {
            try {
                server = new CommonsHttpSolrServer(GlobalConstants.SOLR_URL);
            } catch (MalformedURLException ex) {
                Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return server;
    }

    private Mongo getMongo() {
        if (m == null) {
            try {
                m = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
            } catch (UnknownHostException ex) {
                Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MongoException ex) {
                Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return m;
    }

    public static void initialize() {
        DataSearcher searcher = new DataSearcher();

        //delete everything from solr
        searcher.deleteAllPosts();

        //add all posts from mongo
        Mongo m;
        try {
            m = new Mongo("127.0.0.1", GlobalConstants.MONGODB_PORT);
            DB db = m.getDB("mydb");

            DBCollection collDocs = db.getCollection("docs");
            DBCursor cursor = collDocs.find();
            long start = System.currentTimeMillis();
            List<SolrInputDocument> list = new LinkedList<SolrInputDocument>();
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                list.add(searcher.convertToSolrDocument(object));
            }

            Logger.getLogger(DataSearcher.class.getName()).log(Level.INFO, "documents to index: " + list.size());
            if (list.size() > 0) {
                try {
                    searcher.getSolrServer().add(list);
                    searcher.getSolrServer().commit();
                } catch (SolrServerException ex) {
                    Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Logger.getLogger(DataSearcher.class.getName()).log(Level.INFO, "searcher initialized for " + (System.currentTimeMillis() - start) + " ms");


        } catch (UnknownHostException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoException ex) {
            Logger.getLogger(DataSearcher.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
