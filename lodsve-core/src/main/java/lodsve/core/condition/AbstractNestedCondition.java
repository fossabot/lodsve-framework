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

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for nested conditions.
 *
 * @author Phillip Webb
 */
abstract class AbstractNestedCondition extends SpringBootCondition
		implements ConfigurationCondition {

	private final ConfigurationPhase configurationPhase;

	AbstractNestedCondition(ConfigurationPhase configurationPhase) {
		Assert.notNull(configurationPhase, "ConfigurationPhase must not be null");
		this.configurationPhase = configurationPhase;
	}

	@Override
	public ConfigurationPhase getConfigurationPhase() {
		return this.configurationPhase;
	}

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		String className = getClass().getName();
		MemberConditions memberConditions = new MemberConditions(context, className);
		MemberMatchOutcomes memberOutcomes = new MemberMatchOutcomes(memberConditions);
		return getFinalMatchOutcome(memberOutcomes);
	}

	protected abstract ConditionOutcome getFinalMatchOutcome(
			MemberMatchOutcomes memberOutcomes);

	protected static class MemberMatchOutcomes {

		private final List<ConditionOutcome> all;

		private final List<ConditionOutcome> matches;

		private final List<ConditionOutcome> nonMatches;

		public MemberMatchOutcomes(MemberConditions memberConditions) {
			this.all = Collections.unmodifiableList(memberConditions.getMatchOutcomes());
			List<ConditionOutcome> matches = new ArrayList<ConditionOutcome>();
			List<ConditionOutcome> nonMatches = new ArrayList<ConditionOutcome>();
			for (ConditionOutcome outcome : this.all) {
				(outcome.isMatch() ? matches : nonMatches).add(outcome);
			}
			this.matches = Collections.unmodifiableList(matches);
			this.nonMatches = Collections.unmodifiableList(nonMatches);
		}

		public List<ConditionOutcome> getAll() {
			return this.all;
		}

		public List<ConditionOutcome> getMatches() {
			return this.matches;
		}

		public List<ConditionOutcome> getNonMatches() {
			return this.nonMatches;
		}

	}

	private static class MemberConditions {

		private final ConditionContext context;

		private final MetadataReaderFactory readerFactory;

		private final Map<AnnotationMetadata, List<Condition>> memberConditions;

		MemberConditions(ConditionContext context, String className) {
			this.context = context;
			this.readerFactory = new SimpleMetadataReaderFactory(
					context.getResourceLoader());
			String[] members = getMetadata(className).getMemberClassNames();
			this.memberConditions = getMemberConditions(members);
		}

		private Map<AnnotationMetadata, List<Condition>> getMemberConditions(
				String[] members) {
			MultiValueMap<AnnotationMetadata, Condition> memberConditions = new LinkedMultiValueMap<AnnotationMetadata, Condition>();
			for (String member : members) {
				AnnotationMetadata metadata = getMetadata(member);
				for (String[] conditionClasses : getConditionClasses(metadata)) {
					for (String conditionClass : conditionClasses) {
						Condition condition = getCondition(conditionClass);
						memberConditions.add(metadata, condition);
					}
				}
			}
			return Collections.unmodifiableMap(memberConditions);
		}

		private AnnotationMetadata getMetadata(String className) {
			try {
				return this.readerFactory.getMetadataReader(className)
						.getAnnotationMetadata();
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		@SuppressWarnings("unchecked")
		private List<String[]> getConditionClasses(AnnotatedTypeMetadata metadata) {
			MultiValueMap<String, Object> attributes = metadata
					.getAllAnnotationAttributes(Conditional.class.getName(), true);
			Object values = (attributes != null ? attributes.get("value") : null);
			return (List<String[]>) (values != null ? values : Collections.emptyList());
		}

		private Condition getCondition(String conditionClassName) {
			Class<?> conditionClass = ClassUtils.resolveClassName(conditionClassName,
					this.context.getClassLoader());
			return (Condition) BeanUtils.instantiateClass(conditionClass);
		}

		public List<ConditionOutcome> getMatchOutcomes() {
			List<ConditionOutcome> outcomes = new ArrayList<ConditionOutcome>();
			for (Map.Entry<AnnotationMetadata, List<Condition>> entry : this.memberConditions
					.entrySet()) {
				AnnotationMetadata metadata = entry.getKey();
				List<Condition> conditions = entry.getValue();
				outcomes.add(new MemberOutcomes(this.context, metadata, conditions)
						.getUltimateOutcome());
			}
			return Collections.unmodifiableList(outcomes);
		}

	}

	private static class MemberOutcomes {

		private final ConditionContext context;

		private final AnnotationMetadata metadata;

		private final List<ConditionOutcome> outcomes;

		MemberOutcomes(ConditionContext context, AnnotationMetadata metadata,
				List<Condition> conditions) {
			this.context = context;
			this.metadata = metadata;
			this.outcomes = new ArrayList<ConditionOutcome>(conditions.size());
			for (Condition condition : conditions) {
				this.outcomes.add(getConditionOutcome(metadata, condition));
			}
		}

		private ConditionOutcome getConditionOutcome(AnnotationMetadata metadata,
				Condition condition) {
			if (condition instanceof SpringBootCondition) {
				return ((SpringBootCondition) condition).getMatchOutcome(this.context,
						metadata);
			}
			return new ConditionOutcome(condition.matches(this.context, metadata),
					ConditionMessage.empty());
		}

		public ConditionOutcome getUltimateOutcome() {
			ConditionMessage.Builder message = ConditionMessage
					.forCondition("NestedCondition on "
							+ ClassUtils.getShortName(this.metadata.getClassName()));
			if (this.outcomes.size() == 1) {
				ConditionOutcome outcome = this.outcomes.get(0);
				return new ConditionOutcome(outcome.isMatch(),
						message.because(outcome.getMessage()));
			}
			List<ConditionOutcome> match = new ArrayList<ConditionOutcome>();
			List<ConditionOutcome> nonMatch = new ArrayList<ConditionOutcome>();
			for (ConditionOutcome outcome : this.outcomes) {
				(outcome.isMatch() ? match : nonMatch).add(outcome);
			}
			if (nonMatch.isEmpty()) {
				return ConditionOutcome
						.match(message.found("matching nested conditions").items(match));
			}
			return ConditionOutcome.noMatch(
					message.found("non-matching nested conditions").items(nonMatch));
		}

	}

}
