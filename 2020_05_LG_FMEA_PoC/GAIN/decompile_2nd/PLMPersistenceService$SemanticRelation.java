package com.dassault_systemes.sfy.persistence;

public enum PLMPersistenceService$SemanticRelation {
	FallibleReferenceStandsForReference3(410, 5), FailureModesEffectsAnalysisCauseFailureModeReference3(422,
			5), FailureModesEffectsAnalysisCauseOccurrenceReference3(423,
					5), FailureModesEffectsAnalysisEffectsWithoutBarrierReference3(424,
							5), FailureModesEffectsAnalysisEffectsWithBarrierReference3(425,
									5), FailureModesEffectsAnalysisAnalyzesReference3(426,
											5), FailureModesEffectsAnalysisAnalysisContextReference3(427,
													5), FailureModesEffectsAnalysisEffectsWithoutBarrierOccurrenceReference3(
															441,
															5), FailureModesEffectsAnalysisEffectsWithBarrierOccurrenceReference3(
																	442,
																	5), FailureModesEffectsAnalysisDetectionReference3(
																			443,
																			5), FailureModesEffectsAnalysisPreventionReference3(
																					444,
																					5), PLM_ImplementLink_TargetReference3(
																							51,
																							5), PLM_ImplementLink_SourceReference5(
																									52,
																									7), Is_referencing_OperationReference3(
																											453,
																											5), Is_referencing_ProductCharacteristicReference(
																													451,
																													1), Is_referencing_ProcessCharacteristicReference(
																															452,
																															1);

	protected int role;
	protected int semantic;

	private PLMPersistenceService$SemanticRelation(int var3, int var4) {
		this.role = var3;
		this.semantic = var4;
	}
}