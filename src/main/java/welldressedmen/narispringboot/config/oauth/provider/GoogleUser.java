package welldressedmen.narispringboot.config.oauth.provider;

import java.util.Map;

public class GoogleUser implements OAuthUserInfo{
	private Map<String, Object> attribute;
	public GoogleUser(Map<String, Object> attribute) {
		this.attribute = attribute;
	}
	@Override
	public String getProviderId() {
		return (String)attribute.get("providerId");
	}
	@Override
	public String getProvider() {
		return (String)(attribute.get("provider"));
	}
	@Override
	public String getEmail() {
		return (String)attribute.get("email");
	}

	@Override
	public String getName() {
		return (String)attribute.get("name");
	}

}
