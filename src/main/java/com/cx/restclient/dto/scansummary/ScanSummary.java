package com.cx.restclient.dto.scansummary;

import com.cx.restclient.ast.dto.sca.AstScaResults;
import com.cx.restclient.ast.dto.sca.report.AstScaSummaryResults;
import com.cx.restclient.common.CxPARAM;
import com.cx.restclient.configuration.CxScanConfig;
import com.cx.restclient.osa.dto.OSAResults;
import com.cx.restclient.osa.dto.OSASummaryResults;
import com.cx.restclient.sast.dto.SASTResults;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects errors from provided scan results, based on scan config.
 */
public class ScanSummary {
    private final List<ThresholdError> thresholdErrors = new ArrayList<>();
    private final List<Severity> newResultThresholdErrors = new ArrayList<>();
    private final boolean policyViolated;

    public ScanSummary(CxScanConfig config, SASTResults sastResults, OSAResults osaResults, AstScaResults scaResults) {

        addSastThresholdErrors(config, sastResults);
        addDependencyScanThresholdErrors(config, osaResults, scaResults);

        addNewResultThresholdErrors(config, sastResults);

        policyViolated = determinePolicyViolation(config, sastResults, osaResults);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (ThresholdError error : thresholdErrors) {
            // TODO: Include dependency scanner type into the message.
            result.append(String.format("%s severity results are above threshold. Results: %d. Threshold: %d.%n",
                    error.getSeverity().toString().toLowerCase(),
                    error.getValue(),
                    error.getThreshold()));
        }

        for (Severity severity : newResultThresholdErrors) {
            result.append(String.format("One or more new results of %s severity%n", severity.toString().toLowerCase()));
        }

        if (policyViolated) {
            result.append(CxPARAM.PROJECT_POLICY_VIOLATED_STATUS).append("\n");
        }

        return result.toString();
    }

    public List<ThresholdError> getThresholdErrors() {
        return thresholdErrors;
    }

    public boolean hasErrors() {
        return !thresholdErrors.isEmpty() || !newResultThresholdErrors.isEmpty() || policyViolated;
    }

    public boolean isPolicyViolated() {
        return policyViolated;
    }

    public boolean isSastThresholdExceeded() {
        return thresholdErrors.stream().anyMatch(error -> error.getSource() == ErrorSource.SAST);
    }

    public boolean isOsaThresholdExceeded() {
        return thresholdErrors.stream().anyMatch(error -> error.getSource() == ErrorSource.DEPENDENCY_SCANNER);
    }

    public boolean isSastThresholdForNewResultsExceeded() {
        return !newResultThresholdErrors.isEmpty();
    }

    private void addSastThresholdErrors(CxScanConfig config, SASTResults sastResults) {
        if (config.isSASTThresholdEffectivelyEnabled() &&
                sastResults != null &&
                sastResults.isSastResultsReady()) {
            checkForThresholdError(sastResults.getHigh(), config.getSastHighThreshold(), ErrorSource.SAST, Severity.HIGH);
            checkForThresholdError(sastResults.getMedium(), config.getSastMediumThreshold(), ErrorSource.SAST, Severity.MEDIUM);
            checkForThresholdError(sastResults.getLow(), config.getSastLowThreshold(), ErrorSource.SAST, Severity.LOW);
        }
    }

    private void addDependencyScanThresholdErrors(CxScanConfig config, OSAResults osaResults, AstScaResults scaResults ) {
        if (config.isOSAThresholdEffectivelyEnabled() && (scaResults != null) || osaResults!= null) {

            int totalHigh = 0;
            int totalMedium = 0;
            int totalLow = 0;
            boolean hasSummary = false;

            if (scaResults != null) {
                AstScaSummaryResults summary = scaResults.getSummary();
                if (summary != null) {
                    hasSummary = true;
                    totalHigh = summary.getHighVulnerabilityCount();
                    totalMedium = summary.getMediumVulnerabilityCount();
                    totalLow = summary.getLowVulnerabilityCount();
                }
            } else if (osaResults.isOsaResultsReady()) {
                OSASummaryResults summary = osaResults.getResults();
                if (summary != null) {
                    hasSummary = true;
                    totalHigh = summary.getTotalHighVulnerabilities();
                    totalMedium = summary.getTotalMediumVulnerabilities();
                    totalLow = summary.getTotalLowVulnerabilities();
                }
            }

            if (hasSummary) {
                checkForThresholdError(totalHigh, config.getOsaHighThreshold(), ErrorSource.DEPENDENCY_SCANNER, Severity.HIGH);
                checkForThresholdError(totalMedium, config.getOsaMediumThreshold(), ErrorSource.DEPENDENCY_SCANNER, Severity.MEDIUM);
                checkForThresholdError(totalLow, config.getOsaLowThreshold(), ErrorSource.DEPENDENCY_SCANNER, Severity.LOW);
            }
        }
    }

    private void addNewResultThresholdErrors(CxScanConfig config, SASTResults sastResults) {
        if (sastResults != null && sastResults.isSastResultsReady() && config.getSastNewResultsThresholdEnabled()) {
            String severity = config.getSastNewResultsThresholdSeverity();

            if ("LOW".equals(severity)) {
                if (sastResults.getNewLow() > 0) {
                    newResultThresholdErrors.add(Severity.LOW);
                }
                severity = "MEDIUM";
            }

            if ("MEDIUM".equals(severity)) {
                if (sastResults.getNewMedium() > 0) {
                    newResultThresholdErrors.add(Severity.MEDIUM);
                }
                severity = "HIGH";
            }

            if ("HIGH".equals(severity) && sastResults.getNewHigh() > 0) {
                newResultThresholdErrors.add(Severity.HIGH);
            }
        }
    }

    private static boolean determinePolicyViolation(CxScanConfig config, SASTResults sastResults , OSAResults osaResults ) {



        return config.getEnablePolicyViolations() &&
                ((osaResults != null &&
                     !osaResults.getOsaPolicies().isEmpty()) ||
                        (sastResults != null && !sastResults.getSastPolicies().isEmpty()));
    }

    private void checkForThresholdError(int value, Integer threshold, ErrorSource source, Severity severity) {
        if (threshold != null && value > threshold) {
            ThresholdError error = new ThresholdError(source, severity, value, threshold);
            thresholdErrors.add(error);
        }
    }
}
