package com.scriptglance.data.remote

import com.scriptglance.data.model.ApiResponse
import com.scriptglance.data.model.MobileSocialLoginRequest
import com.scriptglance.data.model.auth.ForgotPasswordRequest
import com.scriptglance.data.model.auth.LoginRequest
import com.scriptglance.data.model.auth.RegisterRequest
import com.scriptglance.data.model.auth.ResetPasswordRequest
import com.scriptglance.data.model.auth.SendVerificationEmailRequest
import com.scriptglance.data.model.auth.TokenResponse
import com.scriptglance.data.model.auth.VerifyEmailRequest
import com.scriptglance.data.model.presentation.AcceptInvitationsResponse
import com.scriptglance.data.model.presentation.InvitationResponse
import com.scriptglance.data.model.presentation.Participant
import com.scriptglance.data.model.presentation.Presentation
import com.scriptglance.data.model.presentation.PresentationActiveData
import com.scriptglance.data.model.presentation.PresentationItem
import com.scriptglance.data.model.presentation.PresentationStats
import com.scriptglance.data.model.presentation.PresentationStructure
import com.scriptglance.data.model.presentation.PresentationsConfig
import com.scriptglance.data.model.presentation.UpdatePresentationRequest
import com.scriptglance.data.model.profile.UserProfile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<TokenResponse?>>

    @POST("/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiResponse<TokenResponse?>>

    @POST("/auth/mobile-social-login")
    suspend fun mobileSocialLogin(@Body body: MobileSocialLoginRequest): Response<ApiResponse<TokenResponse?>>

    @POST("/auth/send-verification-email")
    suspend fun sendVerificationEmail(@Body body: SendVerificationEmailRequest): Response<ApiResponse<Unit?>>

    @POST("/auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<ApiResponse<Unit?>>

    @POST("/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ApiResponse<Unit?>>

    @POST("/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ApiResponse<Unit?>>

    @GET("/presentations/stats")
    suspend fun getStats(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PresentationStats?>>

    @GET("/presentations")
    suspend fun getPresentations(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null,
        @Query("owner") owner: String? = null,
        @Query("lastChange") lastChange: String? = null,
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<PresentationItem>?>>


    @POST("/presentations")
    suspend fun createPresentation(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Presentation?>>

    @GET("/presentations/{id}")
    suspend fun getPresentation(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ApiResponse<Presentation?>>

    @PUT("/presentations/{id}")
    suspend fun updatePresentationName(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: UpdatePresentationRequest
    ): Response<ApiResponse<Presentation?>>

    @DELETE("/presentations/{id}")
    suspend fun deletePresentation(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ApiResponse<Unit?>>

    @GET("/presentations/{presentationId}/participants")
    suspend fun getParticipants(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Long
    ): Response<ApiResponse<List<Participant>?>>


    @DELETE("/presentations/participants/{participantId}")
    suspend fun deleteParticipant(
        @Header("Authorization") token: String,
        @Path("participantId") participantId: Long
    ): Response<ApiResponse<Unit?>>

    @POST("/presentations/{presentationId}/invite")
    suspend fun inviteParticipant(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Long
    ): Response<ApiResponse<InvitationResponse?>>

    @POST("/presentations/invitations/{invitationToken}/accept")
    suspend fun acceptInvitation(
        @Header("Authorization") token: String,
        @Path("invitationToken") invitationToken: String
    ): Response<ApiResponse<AcceptInvitationsResponse?>>

    @GET("/presentations/{id}/structure")
    suspend fun getPresentationStructure(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ApiResponse<PresentationStructure?>>

    @GET("/user/config")
    suspend fun getConfig(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PresentationsConfig?>>

    @GET("/presentations/{presentationId}/active")
    suspend fun getActivePresentation(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Long
    ): Response<ApiResponse<PresentationActiveData?>>

    @GET("/user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ApiResponse<UserProfile?>>

    @Multipart
    @PUT("/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part parts: List<MultipartBody.Part>
    ): Response<ApiResponse<UserProfile?>>
}