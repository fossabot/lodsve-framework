/*
 * Copyright (C) 2019 Sun.Hao(https://www.crazy-coder.cn/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lodsve.core.condition;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * Records condition evaluation details for reporting and logging.
 *
 * @author Greg Turnquist
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public final class ConditionEvaluationReport {

    private static final String BEAN_NAME = "autoConfigurationReport";

    private static final AncestorsMatchedCondition ANCESTOR_CONDITION = new AncestorsMatchedCondition();

    private final SortedMap<String, ConditionAndOutcomes> outcomes = new TreeMap<String, ConditionAndOutcomes>();

    private boolean addedAncestorOutcomes;

    private ConditionEvaluationReport parent;

    private List<String> exclusions = Collections.emptyList();

    private Set<String> unconditionalClasses = new HashSet<String>();

    /**
     * Private constructor.
     *
     * @see #get(ConfigurableListableBeanFactory)
     */
    private ConditionEvaluationReport() {
    }

    /**
     * Obtain a {@link ConditionEvaluationReport} for the specified bean factory.
     *
     * @param beanFactory the bean factory
     * @return an existing or new {@link ConditionEvaluationReport}
     */
    public static ConditionEvaluationReport get(
            ConfigurableListableBeanFactory beanFactory) {
        synchronized (beanFactory) {
            ConditionEvaluationReport report;
            if (beanFactory.containsSingleton(BEAN_NAME)) {
                report = beanFactory.getBean(BEAN_NAME, ConditionEvaluationReport.class);
            } else {
                report = new ConditionEvaluationReport();
                beanFactory.registerSingleton(BEAN_NAME, report);
            }
            locateParent(beanFactory.getParentBeanFactory(), report);
            return report;
        }
    }

    private static void locateParent(BeanFactory beanFactory,
                                     ConditionEvaluationReport report) {
        if (beanFactory != null && report.parent == null
                && beanFactory.containsBean(BEAN_NAME)) {
            report.parent = beanFactory.getBean(BEAN_NAME,
                    ConditionEvaluationReport.class);
        }
    }

    /**
     * Record the occurrence of condition evaluation.
     *
     * @param source    the source of the condition (class or method name)
     * @param condition the condition evaluated
     * @param outcome   the condition outcome
     */
    public void recordConditionEvaluation(String source, Condition condition,
                                          ConditionOutcome outcome) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(condition, "Condition must not be null");
        Assert.notNull(outcome, "Outcome must not be null");
        this.unconditionalClasses.remove(source);
        if (!this.outcomes.containsKey(source)) {
            this.outcomes.put(source, new ConditionAndOutcomes());
        }
        this.outcomes.get(source).add(condition, outcome);
        this.addedAncestorOutcomes = false;
    }

    /**
     * Records the names of the classes that have been excluded from condition evaluation.
     *
     * @param exclusions the names of the excluded classes
     */
    public void recordExclusions(Collection<String> exclusions) {
        Assert.notNull(exclusions, "exclusions must not be null");
        this.exclusions = new ArrayList<String>(exclusions);
    }

    /**
     * Records the names of the classes that are candidates for condition evaluation.
     *
     * @param evaluationCandidates the names of the classes whose conditions will be
     *                             evaluated
     */
    public void recordEvaluationCandidates(List<String> evaluationCandidates) {
        Assert.notNull(evaluationCandidates, "evaluationCandidates must not be null");
        this.unconditionalClasses = new HashSet<String>(evaluationCandidates);
    }

    /**
     * Returns condition outcomes from this report, grouped by the source.
     *
     * @return the condition outcomes
     */
    public Map<String, ConditionAndOutcomes> getConditionAndOutcomesBySource() {
        if (!this.addedAncestorOutcomes) {
            for (Entry<String, ConditionAndOutcomes> entry : this.outcomes
                    .entrySet()) {
                if (!entry.getValue().isFullMatch()) {
                    addNoMatchOutcomeToAncestors(entry.getKey());
                }
            }
            this.addedAncestorOutcomes = true;
        }
        return Collections.unmodifiableMap(this.outcomes);
    }

    private void addNoMatchOutcomeToAncestors(String source) {
        String prefix = source + "$";
        for (Entry<String, ConditionAndOutcomes> entry : this.outcomes.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                ConditionOutcome outcome = ConditionOutcome.noMatch(ConditionMessage
                        .forCondition("Ancestor " + source).because("did not match"));
                entry.getValue().add(ANCESTOR_CONDITION, outcome);
            }
        }
    }

    /**
     * Returns the names of the classes that have been excluded from condition evaluation.
     *
     * @return the names of the excluded classes
     */
    public List<String> getExclusions() {
        return Collections.unmodifiableList(this.exclusions);
    }

    /**
     * Returns the names of the classes that were evaluated but were not conditional.
     *
     * @return the names of the unconditional classes
     */
    public Set<String> getUnconditionalClasses() {
        return Collections.unmodifiableSet(this.unconditionalClasses);
    }

    /**
     * The parent report (from a parent BeanFactory if there is one).
     *
     * @return the parent report (or null if there isn't one)
     */
    public ConditionEvaluationReport getParent() {
        return this.parent;
    }

    /**
     * Provides access to a number of {@link ConditionAndOutcome} items.
     */
    public static class ConditionAndOutcomes implements Iterable<ConditionAndOutcome> {

        private final Set<ConditionAndOutcome> outcomes = new LinkedHashSet<ConditionAndOutcome>();

        public void add(Condition condition, ConditionOutcome outcome) {
            this.outcomes.add(new ConditionAndOutcome(condition, outcome));
        }

        /**
         * Return {@code true} if all outcomes match.
         *
         * @return {@code true} if a full match
         */
        public boolean isFullMatch() {
            for (ConditionAndOutcome conditionAndOutcomes : this) {
                if (!conditionAndOutcomes.getOutcome().isMatch()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Iterator<ConditionAndOutcome> iterator() {
            return Collections.unmodifiableSet(this.outcomes).iterator();
        }

    }

    /**
     * Provides access to a single {@link Condition} and {@link ConditionOutcome}.
     */
    public static class ConditionAndOutcome {

        private final Condition condition;

        private final ConditionOutcome outcome;

        public ConditionAndOutcome(Condition condition, ConditionOutcome outcome) {
            this.condition = condition;
            this.outcome = outcome;
        }

        public Condition getCondition() {
            return this.condition;
        }

        public ConditionOutcome getOutcome() {
            return this.outcome;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ConditionAndOutcome other = (ConditionAndOutcome) obj;
            return (ObjectUtils.nullSafeEquals(this.condition.getClass(),
                    other.condition.getClass())
                    && ObjectUtils.nullSafeEquals(this.outcome, other.outcome));
        }

        @Override
        public int hashCode() {
            return this.condition.getClass().hashCode() * 31 + this.outcome.hashCode();
        }

        @Override
        public String toString() {
            return this.condition.getClass() + " " + this.outcome;
        }

    }

    private static class AncestorsMatchedCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            throw new UnsupportedOperationException();
        }

    }

}
