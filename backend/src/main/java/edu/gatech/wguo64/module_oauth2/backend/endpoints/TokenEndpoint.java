package edu.gatech.wguo64.module_oauth2.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import edu.gatech.wguo64.module_oauth2.backend.entities.Token;
import edu.gatech.wguo64.module_oauth2.backend.utities.Constants;

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
public class TokenEndpoint {
    private Logger logger = Logger.getLogger(TokenEndpoint.class.getName());

    static {
        ObjectifyService.register(Token.class);
    }

    @ApiMethod(
            name = "token.register",
            path = "token/register",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public void registerToken(@Nonnull @Named("token") String token, User user)
            throws OAuthRequestException {
        if (user == null) {
            logger.exiting(TokenEndpoint.class.toString(), "Not logged" +
                    " " +
                    "in.");
            throw new OAuthRequestException("You need to login to register " +
                    "tokens.");
        }
        Token tokenEntity = ofy().load().type(Token.class).filter("userId", user.getUserId()).first().now();
        if(tokenEntity == null) {
            tokenEntity = new Token(token, user.getUserId());
        } else {
            tokenEntity.setToken(token);
        }
        ofy().save().entities(tokenEntity).now();
    }
}
