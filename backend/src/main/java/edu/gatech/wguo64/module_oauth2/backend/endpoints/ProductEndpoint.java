package edu.gatech.wguo64.module_oauth2.backend.endpoints;


import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import static com.googlecode.objectify.ObjectifyService.ofy;

import edu.gatech.wguo64.module_oauth2.backend.entities.Token;
import edu.gatech.wguo64.module_oauth2.backend.utities.Constants;
import edu.gatech.wguo64.module_oauth2.backend.entities.Product;
import edu.gatech.wguo64.module_oauth2.backend.utities.NotificationHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
/**
 * Created by guoweidong on 12/29/15.
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.module_oauth2.wguo64.gatech.edu",
                ownerName = "backend.module_oauth2.wguo64.gatech.edu",
                packagePath = ""
        ),
        scopes = {Constants.EMAIL_SCOPE, Constants.PROFILE_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID,
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class ProductEndpoint {
    private Logger logger = Logger.getLogger(ProductEndpoint.class.getName());
    private final static Integer LIST_LIMIT = 5;
    static {
        ObjectifyService.register(Product.class);
    }
    @ApiMethod(
            name = "product.test",
            path = "product/test",
            httpMethod = ApiMethod.HttpMethod.GET)
    public void test() {
        QueryResultIterator<Token> iterator = ofy().load().type(Token.class).chunkAll().iterator();
        Set<String> userIds = new HashSet<>();
        while(iterator.hasNext()) {
            userIds.add(iterator.next().getUserId());
        }
        NotificationHelper.notify(userIds, "Just for test");
    }

    @ApiMethod(
            name = "product.insert",
            path = "product/insert",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Product insert(Product product, User user) throws OAuthRequestException, BadRequestException {
        if (user == null) {

            logger.exiting(ProductEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to file " +
                    "reports.");
        }
        if(product.getId() != null) {
            throw new BadRequestException("Invalid product");
        }

        Map<Key<Product>, Product> map = ofy().save().entities(product).now();
        if(map != null) {
            Key<Product> key = map.keySet().iterator().next();
            if(ofy().load().type(Product.class).count() % 5 == 0) {
//                Queue queue = QueueFactory.getQueue(ProductEndpoint.class.getName());
//                queue.add(TaskOptions.Builder.withPayload
//                        (new UpdateNotifyDeferredTask()));
                QueryResultIterator<Token> iterator = ofy().load().type(Token.class).chunkAll().iterator();
                Set<String> userIds = new HashSet<>();
                while(iterator.hasNext()) {
                    userIds.add(iterator.next().getUserId());
                }
                NotificationHelper.notify(userIds, "5 more products have been added");
            }
            return ofy().load().key(key).now();
        }
        return null;
    }
    @ApiMethod(
            name = "product.list",
            path = "product/list",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Product> list(@Nullable @Named("cursor") String cursor,
                                            @Nullable @Named("limit") Integer limit,
                                            User user) throws OAuthRequestException {
        if (user == null) {
            logger.exiting(ProductEndpoint.class.toString(), "Not logged " +
                    "in.");
            throw new OAuthRequestException("You need to login to list your " +
                    "reports.");
        }
        limit = limit == null ? LIST_LIMIT : limit;
        Query<Product> query = ofy().load().type(Product.class).limit(limit);
        if(cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Product> iterator = query.iterator();
        ArrayList<Product> res = new ArrayList<>();
        while(iterator.hasNext()) {
            res.add(iterator.next());
        }
        return CollectionResponse.<Product>builder().setNextPageToken(iterator.getCursor().toWebSafeString()).setItems(res).build();
    }

    private static class UpdateNotifyDeferredTask implements DeferredTask {
        public UpdateNotifyDeferredTask() {
            super();
        }

        @Override
        public void run() {
            QueryResultIterator<Token> iterator = ofy().load().type(Token.class).chunkAll().iterator();
            Set<String> userIds = new HashSet<>();
            while(iterator.hasNext()) {
                userIds.add(iterator.next().getUserId());
            }
            NotificationHelper.notify(userIds, "5 more products have been added");
        }
    }
}
