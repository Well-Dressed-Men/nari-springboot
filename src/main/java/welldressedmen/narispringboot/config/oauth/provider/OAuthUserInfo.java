package welldressedmen.narispringboot.config.oauth.provider;

public interface OAuthUserInfo {
	String getProviderId();
	String getProvider();
	String getEmail();
	String getName();
}
