/*
 * Copyright 2009 Google Inc.
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

package com.google.devtools.simple.compiler.scanner;

/**
 * Defines the keywords of the Simple language.
 * 
 * <p>Note that we use our own hashtable implementation here for maximal
 * performance of the scanner.
 *
 * @author Herbert Czymontek
 */
final class Keywords {

  /*
   *  Maps a keyword to its corresponding token.
   */
  private static class KeywordRecord {
    // Next keyword in hashtable
    private KeywordRecord next;

    // Keyword to token mapping
    private final String keyword;
    private final TokenKind tokenKind;

    KeywordRecord(String keyword, TokenKind tokenKind) {
      this.keyword = keyword;
      this.tokenKind = tokenKind;
    }
  }

  // Keyword hash table
  private KeywordRecord[] keywords;

  // Keyword hash table size: must be a power of 2.
  private static final int HASHTABLE_SIZE = 1 << 8;

  /**
   * Initializes the keyword hash table.
   */
  protected Keywords() {
    keywords = new KeywordRecord[HASHTABLE_SIZE];

    // Simple properties section keywords
    enterKeyword(TokenKind.TOK_$AS);
    enterKeyword(TokenKind.TOK_$DEFINE);
    enterKeyword(TokenKind.TOK_$END);
    enterKeyword(TokenKind.TOK_$FORM);
    enterKeyword(TokenKind.TOK_$INTERFACE);
    enterKeyword(TokenKind.TOK_$OBJECT);
    enterKeyword(TokenKind.TOK_$PROPERTIES);
    enterKeyword(TokenKind.TOK_$SOURCE);

    // Simple language keywords
    enterKeyword(TokenKind.TOK_ALIAS);
    enterKeyword(TokenKind.TOK_AND);
    enterKeyword(TokenKind.TOK_AS);
    enterKeyword(TokenKind.TOK_BOOLEAN);
    enterKeyword(TokenKind.TOK_BYREF);
    enterKeyword(TokenKind.TOK_BYTE);
    enterKeyword(TokenKind.TOK_BYVAL);
    enterKeyword(TokenKind.TOK_CASE);
    enterKeyword(TokenKind.TOK_CONST);
    enterKeyword(TokenKind.TOK_DATE);
    enterKeyword(TokenKind.TOK_DIM);
    enterKeyword(TokenKind.TOK_DO);
    enterKeyword(TokenKind.TOK_DOUBLE);
    enterKeyword(TokenKind.TOK_EACH);
    enterKeyword(TokenKind.TOK_ELSE);
    enterKeyword(TokenKind.TOK_ELSEIF);
    enterKeyword(TokenKind.TOK_END);
    enterKeyword(TokenKind.TOK_ERROR);
    enterKeyword(TokenKind.TOK_EVENT);
    enterKeyword(TokenKind.TOK_EXIT);
    enterKeyword(TokenKind.TOK_FOR);
    enterKeyword(TokenKind.TOK_FUNCTION);
    enterKeyword(TokenKind.TOK_GET);
    enterKeyword(TokenKind.TOK_IF);
    enterKeyword(TokenKind.TOK_IN);
    enterKeyword(TokenKind.TOK_INTEGER);
    enterKeyword(TokenKind.TOK_IS);
    enterKeyword(TokenKind.TOK_ISNOT);
    enterKeyword(TokenKind.TOK_LIKE);
    enterKeyword(TokenKind.TOK_LONG);
    enterKeyword(TokenKind.TOK_ME);
    enterKeyword(TokenKind.TOK_MOD);
    enterKeyword(TokenKind.TOK_NEXT);
    enterKeyword(TokenKind.TOK_NEW);
    enterKeyword(TokenKind.TOK_NOT);
    enterKeyword(TokenKind.TOK_NOTHING);
    enterKeyword(TokenKind.TOK_OBJECT);
    enterKeyword(TokenKind.TOK_ON);
    enterKeyword(TokenKind.TOK_OR);
    enterKeyword(TokenKind.TOK_PROPERTY);
    enterKeyword(TokenKind.TOK_RAISEEVENT);
    enterKeyword(TokenKind.TOK_SELECT);
    enterKeyword(TokenKind.TOK_SET);
    enterKeyword(TokenKind.TOK_SHORT);
    enterKeyword(TokenKind.TOK_SINGLE);
    enterKeyword(TokenKind.TOK_STATIC);
    enterKeyword(TokenKind.TOK_STEP);
    enterKeyword(TokenKind.TOK_STRING);
    enterKeyword(TokenKind.TOK_SUB);
    enterKeyword(TokenKind.TOK_THEN);
    enterKeyword(TokenKind.TOK_TO);
    enterKeyword(TokenKind.TOK_TYPEOF);
    enterKeyword(TokenKind.TOK_UNTIL);
    enterKeyword(TokenKind.TOK_VARIANT);
    enterKeyword(TokenKind.TOK_WHILE);
    enterKeyword(TokenKind.TOK_XOR);

    // The following aren't really keywords but it helps to define them as such
    // when detecting the corresponding literals in the scanner (note that the
    // scanner will never return these tokens!)
    enterKeyword(TokenKind.TOK_FALSE);
    enterKeyword(TokenKind.TOK_TRUE);
  }

  /*
   * Computes the hashcode for identifiers to be checked to be keywords.
   */
  private int hashCode(String str) {
    // This is why the hash table size needed to be a power of 2 - so that we can easily and
    // quickly compute the hash code.
    return str.hashCode() & (HASHTABLE_SIZE - 1);
  }

  /*
   * Enter a keyword and its associated token into the keyword hash table.
   */
  private void enterKeyword(TokenKind tokenKind) {
    String keyword = tokenKind.toString();
    int hash = hashCode(keyword);
    KeywordRecord kr = new KeywordRecord(keyword, tokenKind);
    kr.next = keywords[hash];
    keywords[hash] = kr;
  }

  /**
   * Checks whether an identifier is a keyword. If so, then returns the
   * keyword's token, otherwise the generic identifier token is returned.
   * 
   * @param identifier  identifier to check
   * @return  proper token for identifier
   */
  protected TokenKind checkForKeyword(String identifier) {
    // Simple hash table lookup to check for a match of the identifier against any of the 
    // keywords in the keyword hashtable.
    for (KeywordRecord kr = keywords[hashCode(identifier)]; kr != null; kr = kr.next) {
      if (kr.keyword.equals(identifier)) {
        return kr.tokenKind;
      }
    }

    // No keyword found. It's just an identifier.
    return TokenKind.TOK_IDENTIFIER;
  }
}
