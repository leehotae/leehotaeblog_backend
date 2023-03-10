package com.example.demo.Auth;



import java.io.IOException;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.Auth.CustomUserDetails.CustomUserDetails;
import com.example.demo.Auth.Dto.AccountGetDto;
import com.example.demo.Auth.Dto.AccountPutDto;
import com.example.demo.Auth.Dto.ChangePasswordPostDto;
import com.example.demo.Auth.Dto.ChangePasswordPutDto;
import com.example.demo.CommonDto.ResponseDto;
import com.example.demo.CommonException.CustomException;
import com.example.demo.User.Dto.UserSignRequsetDto;
import com.example.demo.User.Dto.UserSignResponseDto;
import com.example.demo.User.Validation.ValidationSequence;
import com.example.demo.Web.Host.HostEnviroment;
import com.example.demo.Web.Jwt.AccessTokenProperties;
import com.example.demo.Web.Jwt.RefreshTokenProperties;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

	private final AuthService authService;
	
	
	@CrossOrigin
	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Validated(ValidationSequence.class) @RequestBody UserSignRequsetDto userdto) throws CustomException
	{

		authService.signup(userdto);

		return new ResponseEntity<>(new ResponseDto<>("???????????? ????????? ????????? ????????? ?????????????????????.\n??????????????? 10????????????.",null),HttpStatus.OK);
		
	}
	
	
	@CrossOrigin
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws CustomException
	{

		
	      ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
	                .path("/")
	                .secure(false)
	                .sameSite("Strict")
	               .httpOnly(true)
	               .maxAge(0)
	               .build();
	      ResponseCookie loginCookie = ResponseCookie.from("login", "")
	                .path("/")
	                .secure(false)
	                .sameSite("Strict")
	               .maxAge(0)
	               .build();

			response.addHeader("Set-Cookie", refreshCookie.toString());
			response.addHeader("Set-Cookie", loginCookie.toString());
		
			return new ResponseEntity<>(new ResponseDto<>("???????????? ??????.",null),HttpStatus.OK);
		
	}
	
	
	
	@CrossOrigin
	@GetMapping("/account")
	public ResponseEntity<?> account( Authentication authentication,HttpServletRequest request) throws CustomException
	{
	
	if (authentication==null)
	{
		
		if(request.getAttribute("error")!=null)
		{
			String error=request.getAttribute("error").toString();
			if (error.equals("expired_accessToken"))
			{
				return new ResponseEntity<>(new ResponseDto<>("ExpiredTokenError", null),HttpStatus.UNAUTHORIZED);
			}

		
	}
		return new ResponseEntity<>(new ResponseDto<>("?????? ?????? ?????? ??????", null),HttpStatus.UNAUTHORIZED);
	}
		CustomUserDetails userDetails=(CustomUserDetails)authentication.getPrincipal();
	
		AccountGetDto dto=new AccountGetDto(userDetails.getUser().getEmail(),userDetails.getUser().getUsername(),userDetails.getUser().getProfileImageUrl());	
		return new ResponseEntity<>(new ResponseDto<>("?????? ?????? ?????? ??????",dto),HttpStatus.OK);
		
	}
	
	
	@CrossOrigin
	@PutMapping("/account")
	public ResponseEntity<?> accountChange( Authentication authentication,  @RequestPart(required=false) MultipartFile image,@RequestPart  @Validated(ValidationSequence.class) AccountPutDto dto) throws CustomException, IllegalStateException, IOException
	{
		
	if (authentication==null)
	{
		return new ResponseEntity<>(new ResponseDto<>("?????? ?????? ?????? ??????", null),HttpStatus.UNAUTHORIZED);
	}
		CustomUserDetails userDetails=(CustomUserDetails)authentication.getPrincipal();
	

	authService.changeAccount(((CustomUserDetails)authentication.getPrincipal()).getUser().getEmail(),dto,image);

		userDetails.getUser().setUsername(dto.getUsername());
		return new ResponseEntity<>(new ResponseDto<>("?????? ?????? ?????? ??????",null),HttpStatus.OK);
		
	}
	
	
	
	
	@CrossOrigin
	@PostMapping("changePassword")
	public ResponseEntity<?> changePassword_post(@Validated(ValidationSequence.class)  @RequestBody ChangePasswordPostDto dto) throws CustomException
	{

		System.out.println(dto);

		authService.changePasswordPost(dto.getEmail());
		return new ResponseEntity<>("???????????? ????????? ????????? ?????? ??????????????????.",HttpStatus.OK);
	}
	

	
	@CrossOrigin
	@PutMapping("changePassword")
	public ResponseEntity<?> changePassword_put(@Validated(ValidationSequence.class) @RequestBody ChangePasswordPutDto dto) throws CustomException
	{


		authService.changePasswordPut(dto);
		return new ResponseEntity<>("???????????? ????????? ????????? ?????? ??????????????????.",HttpStatus.OK);
	}
	

	
	
	
	
	
	
	@CrossOrigin
	@GetMapping("changePassword/{token}")
	public ResponseEntity<?> chnagePassword_get(@PathVariable String token) throws CustomException
	{


		return new ResponseEntity<>("<a target='_blank' href="+HostEnviroment.CLIENT_HOST+"/changepassword/"+token+">???????????? ????????? ??????</a>",HttpStatus.OK);
	}
	
	@CrossOrigin
	@GetMapping("accountVerification/{token}")
	public ResponseEntity<?> verifyAccount(@PathVariable String token) throws CustomException
	{

		authService.verifyAccount(token);
		return new ResponseEntity<>("?????????????????? ??????????????? ?????????????????????.<a target='_blank' href="+HostEnviroment.CLIENT_HOST+"/signin>????????? ??????</a>",HttpStatus.OK);
	}
	
	@CrossOrigin
	@PostMapping("/refreshToken")
	public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws CustomException
	{
		
		Cookie cookies[]=request.getCookies();
		String refreshToken=null;
		DecodedJWT decodeRefreshToken=null;
		
		if (cookies==null)
		{
			System.out.println(cookies);
			return new ResponseEntity<>(new ResponseDto<>("???????????? ???????????????.",null),HttpStatus.FORBIDDEN);
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("refreshToken"))
			{
				refreshToken=cookie.getValue();
			}
			
		}
		
		
		
		if(refreshToken==null)
		{

			return new ResponseEntity<>(new ResponseDto<>("???????????? ???????????????.",null),HttpStatus.FORBIDDEN);
		}
		
		try
		{
			decodeRefreshToken=JWT.require(Algorithm.HMAC512(RefreshTokenProperties.SECRET)).build().verify(refreshToken);
		}
		catch (Exception e) {
		

			return new ResponseEntity<>(new ResponseDto<>("???????????? ???????????????.",null),HttpStatus.FORBIDDEN);
		}
		
		
		String AccessToken=JWT.create()
				.withSubject(decodeRefreshToken.getSubject())
				.withExpiresAt(new Date(System.currentTimeMillis()+AccessTokenProperties.EXPIRATION_TIME))
				.withClaim("id",decodeRefreshToken.getClaim("id").asLong())
				.withClaim("email",decodeRefreshToken.getClaim("email").asString())
				.sign(Algorithm.HMAC512(AccessTokenProperties.SECRET));

		response.addHeader(AccessTokenProperties.HEADER_STRING, AccessTokenProperties.TOKEN_PREFIX+AccessToken);

		return new ResponseEntity<>(new ResponseDto<>("??????????????? ??????",new UserSignResponseDto(decodeRefreshToken.getClaim("id").asLong(),decodeRefreshToken.getClaim("email").asString(),null)),HttpStatus.OK);
		
	}



	
	
	
}


