# WhatsApp Chat Parser - Clojure Implementation

This is a Clojure implementation of the WhatsApp Chat Parser, rewritten from the original Java version.

## Project Structure

```
/workspace
├── deps.edn                                    # Clojure dependencies and configuration
├── src/
│   └── com/prithvianilk/
│       ├── main.clj                            # Main entry point
│       ├── parser_options.clj                  # Parser options (media filtering)
│       ├── whatsapp_message.clj               # Message data structure
│       └── whatsapp_chat_parser.clj           # Main parser logic
├── test/
│   └── com/prithvianilk/
│       └── whatsapp_chat_parser_test.clj      # Unit tests
└── resources/
    ├── example.txt                             # Test resource file
    └── ommited_data.txt                        # Test resource with media

```

## Features

- Parse WhatsApp chat export files
- Handle multi-line messages
- Optional media message filtering
- Lazy sequence-based parsing
- Full unit test coverage

## Requirements

- Clojure 1.11.1 or higher
- Java 8 or higher

## Installation

The project uses `deps.edn` for dependency management. No additional installation is required.

## Usage

### Running the parser

```bash
# Parse a WhatsApp chat file
clojure -M:run /path/to/chat/file.txt
```

### Running tests

```bash
# Run all tests
clojure -M:test
```

### Using as a library

```clojure
(require '[com.prithvianilk.whatsapp-chat-parser :refer [get-messages]]
         '[com.prithvianilk.parser-options :refer [create-options]])

;; Parse all messages
(def messages (get-messages "path/to/chat.txt"))

;; Parse messages and omit media
(def messages-no-media 
  (get-messages "path/to/chat.txt" (create-options true)))

;; Process messages
(doseq [msg messages]
  (println (:by msg) ":" (:content msg)))
```

## Key Components

### whatsapp_message.clj
Defines the `WhatsAppMessage` record with three fields:
- `:timestamp` - Java Instant representing message time
- `:by` - String containing sender name
- `:content` - String containing message content

### parser_options.clj
Defines the `ParserOptions` record with:
- `:omit-media` - Boolean flag to filter out media messages

### whatsapp_chat_parser.clj
Main parser implementation featuring:
- Regex-based message pattern matching
- Lazy sequence processing for memory efficiency
- Multi-line message support
- Date/time parsing using Java time API

### main.clj
Command-line entry point for parsing chat files.

## Implementation Notes

- The parser uses lazy sequences for memory-efficient processing of large chat files
- Date/time parsing uses the system default timezone (same as Java implementation)
- The message pattern matches WhatsApp's standard export format: `[DD/MM/YY, H:MM:SS AM/PM] Name: Content`
- Multi-line messages are properly handled by reading continuation lines

## Differences from Java Implementation

1. **Functional approach**: Uses Clojure's functional programming paradigms instead of OOP
2. **Lazy sequences**: Returns lazy sequences instead of Java Streams
3. **Immutable data**: All data structures are immutable by default
4. **Simpler syntax**: More concise and expressive code
5. **No explicit iterator**: Uses Clojure's sequence abstraction

## Test Coverage

All original Java tests have been ported to Clojure:
- ✓ Basic message parsing with multiple messages
- ✓ Multi-line message support
- ✓ Empty file handling
- ✓ Media filtering with `omit-media` option
- ✓ Stream processing capability

All tests pass successfully.
