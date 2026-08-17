# SmartSpell

SmartSpell is a web-based spelling assistance system developed to understand and demonstrate how real-time spelling suggestions and autocorrection systems work.

The application compares user input against a 370K+ word English dictionary stored in MySQL and provides relevant spelling suggestions using Levenshtein edit distance.

## Tech Stack

- Java
- Java Servlets
- JDBC
- MySQL
- JavaScript
- HTML
- CSS
- Apache Tomcat

## Features

- Real-time spelling suggestions while typing
- Dictionary-based spell checking
- 370K+ English-word dictionary
- Levenshtein edit-distance based word matching
- Multiple spelling suggestions based on similarity
- Clickable suggestions for quick correction
- Complete text spelling correction
- MySQL database integration

## How It Works

1. The user enters text in the text area.
2. JavaScript detects the current word while typing.
3. The word is sent to the Java Servlet.
4. The backend checks the word against the MySQL dictionary.
5. If the word is not found, similar dictionary words are identified using Levenshtein edit distance.
6. The closest matching words are ranked and returned to the frontend.
7. Suggestions are displayed to the user in real time.
8. The user can click a suggestion to replace the misspelled word.

## Database

The application uses MySQL to store the English dictionary.

The dictionary contains approximately **370,000+ words**.

### Table Structure

```text
dictionary_words
├── id
└── word
