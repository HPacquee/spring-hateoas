/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.affordance;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;

/**
 * @author Oliver Gierke
 */
public abstract class Suggestions {

	public static Suggestions NONE = new EmptySuggestions();

	/**
	 * Returns the name of the field that is supposed to be used as human-readable value to symbolize the suggestion.
	 * 
	 * @return the field name. Can be {@literal null}.
	 */
	public abstract String getPromptField();

	/**
	 * Returns the name of the field that us supposed to be sent back to the server to identify the suggestion.
	 * 
	 * @return the field name. Can be {@literal null}.
	 */
	public abstract String getValueField();

	/**
	 * Invokes the given {@link SuggestionHandler} for the {@link Suggestions}. Allows to act on the different suggestion
	 * types explicitly.
	 * 
	 * @param handler must not be {@literal null}.
	 * @return
	 */
	public abstract <T> T accept(SuggestionHandler<T> handler);

	/**
	 * Returns {@link ValueSuggestions} for the given values.
	 * 
	 * @param values
	 * @return
	 */
	public static <S> ValueSuggestions<S> values(S... values) {
		return ValueSuggestions.of(values);
	}

	public static <S> ValueSuggestions<S> values(Collection<S> values) {
		return new ValueSuggestions<S>(values, null, null);
	}

	public static CustomizableSuggestions external(String reference) {
		return ExternalSuggestions.of(reference);
	}

	public static CustomizableSuggestions remote(UriTemplate template) {
		return RemoteSuggestions.of(template);
	}

	public static CustomizableSuggestions remote(Link link) {
		return remote(link.getHref());
	}

	public static CustomizableSuggestions remote(String template) {
		return remote(new UriTemplate(template));
	}

	public static abstract class CustomizableSuggestions extends Suggestions {

		public abstract CustomizableSuggestions withPromptField(String field);

		public abstract CustomizableSuggestions withValueField(String field);
	}

	/**
	 * Suggestions that consist of a static set of values.
	 *
	 * @author Oliver Gierke
	 */
	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ValueSuggestions<T> extends CustomizableSuggestions implements Iterable<T> {

		private final Collection<T> suggestions;
		private final @Wither String promptField, valueField;

		static <T> ValueSuggestions<T> of(T... values) {
			return new ValueSuggestions<T>(Arrays.asList(values), null, null);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.Options#accept(org.springframework.hateoas.affordance.SuggestHandler)
		 */
		@Override
		public <S> S accept(SuggestionHandler<S> handler) {
			return handler.visit(this);
		}

		/* 
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<T> iterator() {
			return suggestions.iterator();
		}
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ExternalSuggestions extends CustomizableSuggestions {

		private final String reference;
		private final @Wither String promptField, valueField;

		static ExternalSuggestions of(String reference) {
			return new ExternalSuggestions(reference, null, null);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.Options#accept(org.springframework.hateoas.affordance.SuggestHandler)
		 */
		@Override
		public <T> T accept(SuggestionHandler<T> handler) {
			return handler.visit(this);
		}
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class RemoteSuggestions extends CustomizableSuggestions {

		private final UriTemplate template;
		private final @Wither String promptField, valueField;

		static RemoteSuggestions of(UriTemplate template) {
			return new RemoteSuggestions(template, null, null);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.Options#accept(org.springframework.hateoas.affordance.SuggestHandler)
		 */
		@Override
		public <T> T accept(SuggestionHandler<T> handler) {
			return handler.visit(this);
		}
	}

	public void accept(SimpleSuggestionHandler handler) {
		accept(new SuggestionHandlerAdapter(handler));
	}

	private static class EmptySuggestions extends Suggestions {

		public String getPromptField() {
			return null;
		}

		public String getValueField() {
			return null;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.Options#accept(org.springframework.hateoas.affordance.OptionsHandler)
		 */
		@Override
		public <T> T accept(SuggestionHandler<T> handler) {
			return null;
		}
	}
}