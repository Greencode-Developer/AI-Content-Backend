package com.ai_content.brandprofile.service;

import com.ai_content.brandprofile.command.UpdateBrandProfileCommand;
import com.ai_content.brandprofile.domain.BrandProfile;
import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandProfileServiceTest {

   private static final Long USER_ID = 2L;
   private static final Long PROFILE_ID = 10L;

   private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 9, 1, 10, 0);

   @Mock
   private BrandProfileRepository repository;

   private BrandProfileService service;

   @BeforeEach
   void setUp() {
      service = new BrandProfileService(
         repository,
         new ProfileCompletenessCalculator()
      );
   }

   @Test
   void getByUserIdShouldReturnProfile() {
      BrandProfile existing = emptyProfile();

      when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

      BrandProfile result = service.getByUserId(USER_ID);

      assertEquals(existing, result);
      verify(repository).findByUserId(USER_ID);
   }

   @Test
   void getByUserIdShouldThrowWhenProfileNotFound() {
      when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      CustomException exception = assertThrows(
         CustomException.class,
         () -> service.getByUserId(USER_ID)
      );

      assertEquals(
         ErrorCode.BRAND_PROFILE_NOT_FOUND,
         exception.getErrorCode()
      );
   }

   @ParameterizedTest
   @CsvSource({
      "20, 5, false, 40",
      "50, 5, false, 50",
      "50, 10, false, 60",
      "50, 10, true, 80",
      "100, 20, true, 100"
   })

   void updateShouldPassDataAndCalculatedScoreRepository(
      int descriptionLength,
      int toneLength,
      boolean hasColors,
      int expectedScore
   ) {
      BrandProfile existing = emptyProfile();

      String description = "a".repeat(descriptionLength);
      String toneOfVoice = "b".repeat(toneLength);
      String forbiddenWords = "cheapest";

      List<String> colors = hasColors ? List.of("#aabbcc", "#FFFFFF") : List.of();

      UpdateBrandProfileCommand command = new UpdateBrandProfileCommand(
         description,
         toneOfVoice,
         forbiddenWords,
         colors
      );

      // Simulate the result returned by the repository after saving
      BrandProfile saved = new BrandProfile(
         PROFILE_ID,
         USER_ID,
         description,
         toneOfVoice,
         forbiddenWords,
         colors,
         expectedScore,
         CREATED_AT,
         CREATED_AT.plusHours(1)
      );

      when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

      when(repository.update(any(BrandProfile.class))).thenReturn(Optional.of(saved));

      BrandProfile result = service.update(USER_ID, command);

      ArgumentCaptor<BrandProfile> captor = ArgumentCaptor.forClass(BrandProfile.class);

      verify(repository).update(captor.capture());

      // Simulate the result returned by the repository after saving
      BrandProfile submitted = captor.getValue();

      assertAll(
         () -> assertEquals(PROFILE_ID, submitted.id()),
         () -> assertEquals(USER_ID, submitted.userId()),
         () -> assertEquals(CREATED_AT, submitted.createdAt()),
         () -> assertEquals(description, submitted.description()),
         () -> assertEquals(toneOfVoice, submitted.toneOfVoice()),
         () -> assertEquals(forbiddenWords, submitted.forbiddenWords()),
         () -> assertEquals(colors, submitted.brandColors()),
         () -> assertEquals(expectedScore, submitted.completenessPct())
      );

      assertSame(saved, result);
      verify(repository).findByUserId(USER_ID);
   }

   @Test
   void updateShouldReduceScoreWhenDescriptionAndColorsAreCleared() {
      String toneOfVoice = "b".repeat(20);

      BrandProfile existing = new BrandProfile(
         PROFILE_ID,
         USER_ID,
         "a".repeat(100),
         toneOfVoice,
         "cheapest",
         List.of("#AABBCC"),
         100,
         CREATED_AT,
         CREATED_AT
      );

      UpdateBrandProfileCommand command = new UpdateBrandProfileCommand(
         null,
         toneOfVoice,
         null,
         List.of()
      );

      when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

      when(repository.update(any(BrandProfile.class))).thenAnswer(invocation -> Optional.of(
         invocation.getArgument(0, BrandProfile.class)
      ));

      BrandProfile result = service.update(USER_ID, command);
      
      ArgumentCaptor<BrandProfile> captor = ArgumentCaptor.forClass(BrandProfile.class);

      verify(repository).update(captor.capture());

      BrandProfile submitted = captor.getValue();

      assertAll(
         () -> assertNull(submitted.description()),
         () -> assertEquals(toneOfVoice, submitted.toneOfVoice()),
         () -> assertNull(submitted.forbiddenWords()),
         () -> assertTrue(submitted.brandColors().isEmpty()),
         () -> assertEquals(40, submitted.completenessPct()),
         () -> assertEquals(PROFILE_ID, submitted.id()),
         () -> assertEquals(USER_ID, submitted.userId()),
         () -> assertEquals(CREATED_AT, submitted.createdAt())
      );

      assertEquals(40, result.completenessPct());
   }

   @Test
   void updateShouldNotWriteWhenProfileNotFound() {
      UpdateBrandProfileCommand command = new UpdateBrandProfileCommand(
         "a".repeat(100),
         "b".repeat(20),
         null,
         List.of("#AABBCC")
      );

      when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

      CustomException exception = assertThrows(
         CustomException.class,
         () -> service.update(USER_ID, command)
      );

      verify(repository, never()).update(any(BrandProfile.class));
   }

   @Test
   void updateShouldThrowWhenRepositoryReturnEmpty() {
      UpdateBrandProfileCommand command = new UpdateBrandProfileCommand(
         "a".repeat(100),
         "b".repeat(20),
         null,
         List.of("#AABBCC")
      );

      when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyProfile()));

      when(repository.update(any(BrandProfile.class))).thenReturn(Optional.empty());

      CustomException exception = assertThrows(
         CustomException.class,
         () -> service.update(USER_ID, command)
      );

      assertEquals(
         ErrorCode.BRAND_PROFILE_NOT_FOUND,
         exception.getErrorCode()
      );

      verify(repository).update(any(BrandProfile.class));
   }

   private BrandProfile emptyProfile() {
      return new BrandProfile(
         PROFILE_ID,
         USER_ID,
         null,
         null,
         null,
         List.of(),
         0,
         CREATED_AT,
         CREATED_AT
      );
   }
}