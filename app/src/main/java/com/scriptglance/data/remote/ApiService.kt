package com.scriptglance.data.remote

import com.scriptglance.data.model.api.ApiResponse
import com.scriptglance.data.model.auth.MobileSocialLoginRequest
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
import com.scriptglance.data.model.profile.User
import com.scriptglance.data.model.teleprompter.ConfirmActiveReaderRequest
import com.scriptglance.data.model.teleprompter.ParticipantVideoCount
import com.scriptglance.data.model.teleprompter.PresentationPartFull
import com.scriptglance.data.model.teleprompter.SetActiveReaderRequest
import com.scriptglance.data.model.teleprompter.SetRecordingModeRequest
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
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<TokenResponse?>>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiResponse<TokenResponse?>>

    @POST("api/auth/mobile-social-login")
    suspend fun mobileSocialLogin(@Body body: MobileSocialLoginRequest): Response<ApiResponse<TokenResponse?>>

    @POST("api/auth/send-verification-email")
    suspend fun sendVerificationEmail(@Body body: SendVerificationEmailRequest): Response<ApiResponse<Unit?>>

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<ApiResponse<Unit?>>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ApiResponse<Unit?>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ApiResponse<Unit?>>

    @GET("api/presentations/stats")
    suspend fun getStats(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PresentationStats?>>

    @GET("api/presentations")
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


    @POST("api/presentations")
    suspend fun createPresentation(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Presentation?>>

    @GET("api/presentations/{id}")
    suspend fun getPresentation(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<Presentation?>>

    @PUT("api/presentations/{id}")
    suspend fun updatePresentationName(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: UpdatePresentationRequest
    ): Response<ApiResponse<Presentation?>>

    @DELETE("api/presentations/{id}")
    suspend fun deletePresentation(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/presentations/{presentationId}/participants")
    suspend fun getParticipants(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int
    ): Response<ApiResponse<List<Participant>?>>


    @DELETE("api/presentations/participants/{participantId}")
    suspend fun deleteParticipant(
        @Header("Authorization") token: String,
        @Path("participantId") participantId: Int
    ): Response<ApiResponse<Unit?>>

    @POST("api/presentations/{presentationId}/invite")
    suspend fun inviteParticipant(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int
    ): Response<ApiResponse<InvitationResponse?>>

    @POST("api/presentations/invitations/{invitationToken}/accept")
    suspend fun acceptInvitation(
        @Header("Authorization") token: String,
        @Path("invitationToken") invitationToken: String
    ): Response<ApiResponse<AcceptInvitationsResponse?>>

    @GET("api/presentations/{id}/structure")
    suspend fun getPresentationStructure(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse<PresentationStructure?>>

    @GET("api/user/config")
    suspend fun getConfig(
        @Header("Authorization") token: String
    ): Response<ApiResponse<PresentationsConfig?>>

    @GET("api/presentations/{presentationId}/active")
    suspend fun getActivePresentation(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int
    ): Response<ApiResponse<PresentationActiveData?>>

    @GET("api/user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ApiResponse<User?>>

    @Multipart
    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part parts: List<MultipartBody.Part>
    ): Response<ApiResponse<User?>>

    @POST("api/presentations/{presentationId}/start")
    suspend fun startPresentation(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int
    ): Response<Unit>

    @POST("api/presentations/{presentationId}/stop")
    suspend fun stopPresentation(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int
    ): Response<Unit>

    @PUT("api/presentations/{presentationId}/recording-mode")
    suspend fun setRecordingMode(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int,
        @Body request: SetRecordingModeRequest
    ): Response<Unit>

    @GET("api/presentations/{presentationId}/participants/videos-left")
    suspend fun getParticipantsVideosLeft(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int
    ): Response<ApiResponse<List<ParticipantVideoCount>?>>

    @PUT("api/presentations/{presentationId}/active/reader")
    suspend fun setActiveReader(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int,
        @Body request: SetActiveReaderRequest
    ): Response<Unit>

    @POST("api/presentations/{presentationId}/active/reader/confirm")
    suspend fun confirmActiveReader(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int,
        @Body request: ConfirmActiveReaderRequest
    ): Response<Unit>

    @GET("api/presentations/{presentationId}/parts")
    suspend fun getParts(
        @Header("Authorization") token: String,
        @Path("presentationId") presentationId: Int
    ): Response<ApiResponse<List<PresentationPartFull>?>>
}