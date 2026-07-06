import { useState } from "react";

import {
  addMonths,
  getCalendarMonth,
  isBeforeMonth,
} from "../../../shared/lib/date";
import {
  createInitialPublishTripDraft,
  publishSteps,
  type PublishTripDraft,
} from "../model/publish-trip-draft";

type UsePublishTripWizardParams = {
  today: Date;
};

export function usePublishTripWizard({ today }: UsePublishTripWizardParams) {
  const calendarMinMonth = new Date(today.getFullYear(), today.getMonth(), 1);
  const [publishStep, setPublishStep] = useState(0);
  const [publishDraft, setPublishDraft] = useState(() =>
    createInitialPublishTripDraft(today),
  );
  const [calendarCursor, setCalendarCursor] = useState(
    () => new Date(calendarMinMonth),
  );

  const currentPublishStep = publishSteps[publishStep] ?? publishSteps[0];

  const isPublishSummaryStep = publishStep === publishSteps.length - 1;

  const visibleCalendarMonths = [
    getCalendarMonth(calendarCursor),
    getCalendarMonth(addMonths(calendarCursor, 1)),
  ];

  const canGoToPreviousCalendarMonth = !isBeforeMonth(
    addMonths(calendarCursor, -1),
    calendarMinMonth,
  );

  const publishProgress = `${(publishStep / (publishSteps.length - 1)) * 100}%`;

  const updatePublishDraft = <Key extends keyof PublishTripDraft>(
    key: Key,
    value: PublishTripDraft[Key],
  ) => {
    setPublishDraft((currentDraft) => ({
      ...currentDraft,
      [key]: value,
    }));
  };

  const selectPublishDate = (dateKey: string, dateLabel: string) => {
    setPublishDraft((currentDraft) => ({
      ...currentDraft,
      date: dateLabel,
      dateKey,
    }));
  };

  const swapRoute = () => {
    setPublishDraft((currentDraft) => ({
      ...currentDraft,
      origin: currentDraft.destination,
      destination: currentDraft.origin,
    }));
  };

  const goToNextPublishStep = () => {
    setPublishStep((currentStep) =>
      Math.min(currentStep + 1, publishSteps.length - 1),
    );
  };

  const goToPreviousPublishStep = () => {
    setPublishStep((currentStep) => Math.max(currentStep - 1, 0));
  };

  const resetPublishWizard = () => {
    setPublishDraft(createInitialPublishTripDraft(today));
    setPublishStep(0);
    setCalendarCursor(new Date(calendarMinMonth));
  };

  const goToPreviousCalendarMonth = () => {
    setCalendarCursor((currentDate) => addMonths(currentDate, -1));
  };

  const goToNextCalendarMonth = () => {
    setCalendarCursor((currentDate) => addMonths(currentDate, 1));
  };

  return {
    publishStep,
    publishDraft,
    currentPublishStep,
    isPublishSummaryStep,
    visibleCalendarMonths,
    canGoToPreviousCalendarMonth,
    publishProgress,
    updatePublishDraft,
    selectPublishDate,
    swapRoute,
    goToNextPublishStep,
    goToPreviousPublishStep,
    resetPublishWizard,
    goToPreviousCalendarMonth,
    goToNextCalendarMonth,
  };
}
