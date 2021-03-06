package jp.co.takawagu.houseworkserver.handler

import jp.co.takawagu.houseworkserver.config.JwtTokenUtil
import jp.co.takawagu.houseworkserver.model.LoginRequest
import jp.co.takawagu.houseworkserver.model.LoginResponse
import jp.co.takawagu.houseworkserver.model.User
import jp.co.takawagu.houseworkserver.repository.SecurityContextRepository
import jp.co.takawagu.houseworkserver.repository.UserRepository
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Controller
class AuthHandler(private val passwordEncoder: PasswordEncoder,
                  private val userRepository: UserRepository,
                  private val jwtTokenUtil: JwtTokenUtil) {
    fun signUp(req: ServerRequest): Mono<ServerResponse> {
        return req
                .bodyToMono(User::class.java)
                .map { it.password = passwordEncoder.encode(it.password)
                        return@map it }
                .flatMap { userRepository.findByUserName(it.userName)
                        .flatMap { ServerResponse.badRequest().build()}
                        .switchIfEmpty(userRepository.save(it).flatMap {
                            savedUser -> ServerResponse.ok().bodyValue(savedUser)
                        })
                }
    }

    fun login(req: ServerRequest): Mono<ServerResponse> {
        val loginRequest = req.bodyToMono(LoginRequest::class.java)
        return loginRequest.flatMap {
            userRepository.findByUserName(it.userName)
                    .flatMap { user ->
                        if(passwordEncoder.matches(it.password, user.password)) {
                            ServerResponse.ok().bodyValue(
                                    LoginResponse(jwtTokenUtil.generateToken(user), user.userId, user.userName, user.color))
                        } else {
                            ServerResponse.badRequest().build()
                        }
                    }.switchIfEmpty(ServerResponse.badRequest().build())
        }
    }

    fun getAllUser(req: ServerRequest) = ServerResponse.ok().body(userRepository.getAllUser(), User::class.java)

    fun getUserInfo(req: ServerRequest): Mono<ServerResponse> {
        val token = req.headers().header(HttpHeaders.AUTHORIZATION)[0].replace(SecurityContextRepository.TOKEN_PREFIX, "")
        val user = userRepository.findByUserName(jwtTokenUtil.getUserNameFromToken(token)).doOnNext { it.password = null }
        return ServerResponse.ok().body(user, User::class.java)
    }
}

