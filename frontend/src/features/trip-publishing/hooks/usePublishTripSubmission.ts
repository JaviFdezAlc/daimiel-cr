import { useState } from 'react'

import {
  createTrip,
  CreateTripError,
} from '../api/create-trip'
import {
  CreateTripRequestMappingError,
  mapPublishTripDraftToCreateTripRequest,
} from '../api/map-publish-trip-draft-to-create-trip-request'
import type { PublishTripDraft } from '../model/publish-trip-draft'

type PublishTripSubmissionParams = {
  draft: PublishTripDraft
  driverId: string
}

type SubmissionStatus =
  | 'idle'
  | 'submitting'
  | 'success'
  | 'error'

export function usePublishTripSubmission() {
  const [status, setStatus] =
    useState<SubmissionStatus>('idle')
  const [errorMessage, setErrorMessage] =
    useState<string | null>(null)
  const [createdTripId, setCreatedTripId] =
    useState<string | null>(null)

  const publishTrip = async ({
    draft,
    driverId,
  }: PublishTripSubmissionParams) => {
    const normalizedDriverId = driverId.trim()

    if (!normalizedDriverId) {
      setStatus('error')
      setErrorMessage(
        'Falta configurar el conductor de pruebas.',
      )
      return false
    }

    setStatus('submitting')
    setErrorMessage(null)
    setCreatedTripId(null)

    try {
      const request =
        mapPublishTripDraftToCreateTripRequest(draft)

      const result = await createTrip({
        driverId: normalizedDriverId,
        request,
      })

      setCreatedTripId(result.tripId)
      setStatus('success')

      return true
    } catch (error: unknown) {
      const message =
        error instanceof CreateTripError ||
        error instanceof CreateTripRequestMappingError
          ? error.message
          : 'No se pudo publicar el viaje.'

      setStatus('error')
      setErrorMessage(message)

      return false
    }
  }

  const clearError = () => {
    if (status === 'error') {
      setStatus('idle')
    }

    setErrorMessage(null)
  }

  const resetSubmission = () => {
    setStatus('idle')
    setErrorMessage(null)
    setCreatedTripId(null)
  }

  return {
    isSubmitting: status === 'submitting',
    isPublished: status === 'success',
    errorMessage,
    createdTripId,
    publishTrip,
    clearError,
    resetSubmission,
  }
}
