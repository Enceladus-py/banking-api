package com.example.demo.common.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Service for generating and validating JWT tokens.
 */
@Service
public class JwtService {

	@Value("${security.jwt.secret-key:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
	private String secretKey;

	@Value("${security.jwt.expiration-time:86400000}")
	private long jwtExpiration;

	@Value("${security.jwt.refresh-expiration-time:604800000}")
	private long refreshExpiration;

	/**
	 * Default constructor.
	 */
	public JwtService() {
	}

	/**
	 * Extracts the username (email) from the JWT token.
	 *
	 * @param token
	 *            the JWT token
	 * @return the username
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extracts a specific claim from the JWT token.
	 *
	 * @param token
	 *            the JWT token
	 * @param claimsResolver
	 *            the function to extract the claim
	 * @param <T>
	 *            the type of the claim
	 * @return the claim
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Generates a JWT access token for the given user details.
	 *
	 * @param userDetails
	 *            the user details
	 * @return the JWT access token
	 */
	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	/**
	 * Generates a JWT refresh token for the given user details.
	 *
	 * @param userDetails
	 *            the user details
	 * @return the JWT refresh token
	 */
	public String generateRefreshToken(UserDetails userDetails) {
		return buildToken(new HashMap<>(), userDetails, refreshExpiration);
	}

	/**
	 * Generates a JWT token with extra claims for the given user details.
	 *
	 * @param extraClaims
	 *            extra claims to include in the token
	 * @param userDetails
	 *            the user details
	 * @return the JWT token
	 */
	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}

	private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
		return Jwts.builder().claims(extraClaims).subject(userDetails.getUsername())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + expiration)).signWith(getSignInKey()).compact();
	}

	/**
	 * Validates the JWT token against the user details.
	 *
	 * @param token
	 *            the JWT token
	 * @param userDetails
	 *            the user details
	 * @return true if the token is valid, false otherwise
	 */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
