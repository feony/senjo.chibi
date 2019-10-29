/* Copyright 2019, Senjo Org. Denis Rezvyakov aka Dinya Feony Senjo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package org.senjo.data;

/**
 * 
 * @author Denis Rezvyakov aka Dinya Feony Senjo
 * @version create 2019-03-14 */
public final class Phrase extends APhrase<Phrase, String> {
	public Phrase() { super(16); }
	@Override protected String apply(CharSequence data) { return data.toString(); }
}


