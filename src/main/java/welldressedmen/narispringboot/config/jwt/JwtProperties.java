package welldressedmen.narispringboot.config.jwt;

public interface JwtProperties {
	String SECRET = "겟인데어"; // 우리 서버만 알고 있는 비밀값
	String REFRESH_SECRET = "리프레시겟인데어"; // 우리 서버만 알고 있는 비밀값
	int EXPIRATION_TIME = 10*60*1000; // 10분(1/1000초)
	int REFRESH_EXPIRATION_TIME = 10*24*60*60*1000; // 10일(1/1000초)
	String TOKEN_TYPE = "Bearer ";
	String HEADER_STRING = "Authorization";
}
