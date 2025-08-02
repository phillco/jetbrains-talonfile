# JetBrains Talon Language Support

![Build](https://github.com/phillco/jetbrains-talonfile/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

<!-- Plugin description -->
A JetBrains IDE plugin that provides comprehensive language support for [Talon](https://talonvoice.com/) files (`.talon`). Talon is a hands-free programming tool that enables voice-controlled coding through customizable voice commands.

This plugin enhances the development experience for Talon users by providing:

- **Syntax Highlighting** - Full syntax coloring for Talon language constructs including commands, contexts, actions, captures, and lists
- **File Type Recognition** - Proper `.talon` file handling with custom icon
- **Code Folding** - Collapse/expand multiline Talon rules and commands
- **Smart Commenting** - Comment entire rules with a single action (Ctrl+Shift+/)
- **Bracket Matching** - Automatic matching of parentheses, brackets, and braces
- **Code Structure** - Understanding of Talon's voice command → action mapping structure

Perfect for developers who use Talon for voice coding and need proper IDE support for editing their Talon configuration files.
<!-- Plugin description end -->

## Features

### Syntax Highlighting
The plugin provides syntax highlighting for all Talon language elements:
- Comments (`#`)
- Context specifications (`app:`, `mode:`, `tag:`, etc.)
- Voice commands and patterns
- Actions and function calls
- Captures (`<user.text>`) and lists (`{user.list}`)
- Keywords, operators, and literals

### Smart Rule Commenting
Use **Ctrl+Shift+/** to intelligently comment/uncomment entire Talon rules:
```talon
# Automatically comments the entire rule
some voice command:
    action_one()
    action_two()
    action_three()
```

### Code Structure Support
- Automatic bracket/brace matching
- Code folding for multiline commands
- Proper indentation handling

## Installation

**Note:** This plugin is not yet available on the JetBrains Marketplace. You'll need to build it yourself and install it manually.

### Building and Installing

1. **Clone the repository:**
   ```bash
   git clone https://github.com/phillco/jetbrains-talonfile.git
   cd jetbrains-talonfile
   ```

2. **Build the plugin:**
   ```bash
   ./gradlew buildPlugin
   ```
   This will create a `.zip` file in `build/distributions/`

3. **Install in your IDE:**
   - Open your JetBrains IDE
   - Go to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
   - Select the `.zip` file from `build/distributions/`
   - Restart your IDE

### Future Installation Methods

Once the plugin is published to the JetBrains Marketplace, you'll be able to install it directly through:
- <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > Search for "Talon Language Support"

## Requirements

- IntelliJ IDEA 2024.3.6 or later (Community or Ultimate)
- Also compatible with other JetBrains IDEs (PyCharm, WebStorm, etc.)

## Development

This plugin is built using:
- Kotlin
- IntelliJ Platform SDK
- Gradle with Kotlin DSL

To build from source:
```bash
./gradlew build
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template