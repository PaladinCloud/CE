package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.accessanalyzer.model.AnalyzerSummary;
import com.amazonaws.services.accessanalyzer.model.FindingSummaryV2;
import com.amazonaws.services.rds.model.Tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * The Class AccessAnalyzerVH.
 */
public class AccessAnalyzerVH {

    /**
     * The analyzer summary.
     */
    private AnalyzerSummary analyzer;

    /**
     * The tags.
     */
    private List<Tag> tags;

    /**
     * finding summary of the analyzer
     */
    private List<FindingSummaryV2> finding;

    /**
     * Instantiates a new AccessAnalyzer instance VH.
     *
     * @param analyzer the analyzer
     * @param finding the findings
     */
    public AccessAnalyzerVH(AnalyzerSummary analyzer, List<FindingSummaryV2> finding) {
        this.analyzer = analyzer;
        this.finding = finding;
        this.tags = new ArrayList<>();
        if (analyzer != null && analyzer.getTags() != null && !analyzer.getTags().isEmpty()) {
            Iterator<Entry<String, String>> it = analyzer.getTags().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                Tag tag = new Tag();
                tag.setKey(entry.getKey());
                tag.setValue(entry.getValue());
                this.tags.add(tag);
            }
        }
    }

    public AnalyzerSummary getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(AnalyzerSummary analyzer) {
        this.analyzer = analyzer;
    }

    public List<FindingSummaryV2> getFinding() {
        return finding;
    }

    public void setFinding(List<FindingSummaryV2> finding) {
        this.finding = finding;
    }

    /**
     * Gets the tags.
     *
     * @return the tags
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Sets the tags.
     *
     * @param tags the new tags
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
