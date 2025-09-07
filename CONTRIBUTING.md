# Contributing to MIDI Guitar Pedal MCP Server

Thank you for your interest in contributing! This project aims to make guitar effects control more accessible through AI assistants.

## 🎯 How to Contribute

### 1. 🎛️ Adding New Pedal Support
The most valuable contributions are adding support for new guitar pedals:

1. **Find MIDI documentation** for your pedal (manual, implementation chart)
2. **Create parameter mappings** following the `MerisLVXLoader.kt` example
3. **Test with mock executor** to verify CC mappings
4. **Submit PR** with your pedal configuration

#### Pedal Contribution Checklist
- [ ] MIDI CC table complete with parameter names
- [ ] Reasonable value ranges (0-127 or restricted ranges)
- [ ] Parameter categories (Global, Delay, Filter, etc.)
- [ ] Clear descriptions for each parameter
- [ ] Test implementation with `MockMidiExecutor`

### 2. 🧪 Testing and Bug Fixes
- Run `mvn test` before submitting changes
- Add tests for new functionality
- Report bugs with clear reproduction steps
- Verify fixes work with real MIDI hardware when possible

### 3. 📚 Documentation
- Improve README examples
- Add usage documentation for new features
- Create tutorials for common workflows
- Document new pedal configurations

### 4. 🚀 New Features
- MIDI learn functionality
- Additional MCP tools
- Preset management
- Real-time monitoring
- Web interface components

## 🛠️ Development Setup

### Prerequisites
- Java 8+
- Maven 3.6+
- Git
- MIDI interface (optional, can use mock)

### Setup Steps
```bash
# Fork and clone
git clone https://github.com/YOUR_USERNAME/midimcp.git
cd midimcp

# Build and test
mvn compile
mvn test

# Run development server
mvn exec:java
```

### Development Workflow
1. **Create feature branch**: `git checkout -b feature/strymon-timeline`
2. **Make changes**: Add pedal configs, tests, documentation
3. **Test thoroughly**: `mvn test` + manual testing
4. **Commit with clear messages**: `git commit -m "Add Strymon Timeline pedal support"`
5. **Push and create PR**: `git push origin feature/strymon-timeline`

## 📋 Code Guidelines

### Code Style
- Follow existing Kotlin conventions
- Use clear, descriptive variable names
- Add comments for complex MIDI logic
- Keep functions focused and testable

### Testing Requirements
- **New pedals**: Test all parameter mappings
- **New features**: Unit tests + integration tests
- **Bug fixes**: Regression test to prevent recurrence
- **MIDI commands**: Verify byte generation is correct

### Example: Adding a New Pedal
```kotlin
// 1. Create parameter mappings
val strymonTimelineParameters = listOf(
    CCParameter("Mix", 1, 0, 127, "Wet/dry mix", "%", "Global"),
    CCParameter("Time", 2, 0, 127, "Delay time", "ms", "Delay"),
    CCParameter("Feedback", 3, 0, 127, "Delay feedback", "%", "Delay"),
    // ... more parameters
)

// 2. Create pedal model
val strymonTimeline = PedalModel(
    id = "strymon_timeline",
    manufacturer = "Strymon", 
    modelName = "Timeline",
    parameters = strymonTimelineParameters,
    description = "Strymon Timeline delay pedal"
)

// 3. Test with mock executor
@Test
fun testStrymonTimelineCommands() {
    val command = MidiCommand(1, 2, 80, "Time", "Medium delay time")
    val result = mockExecutor.executeCommand(command)
    assertTrue(result.success)
    assertEquals("B0 02 50", result.midiBytes)
}
```

## 🎵 Pedal Priority List

Help us support these popular pedals:

### High Priority
- [ ] **Strymon Timeline** - Professional delay
- [ ] **Eventide TimeFactor** - Studio-grade delay  
- [ ] **Boss DD-500** - Digital delay workstation
- [ ] **TC Electronic Flashback** - Popular delay pedal

### Medium Priority  
- [ ] **Strymon BigSky** - Reverb pedal
- [ ] **Boss RV-500** - Reverb workstation
- [ ] **Chase Bliss Tonal Recall** - Analog delay

### Low Priority
- [ ] Overdrive/distortion pedals
- [ ] Modulation effects (chorus, flanger)
- [ ] Multi-effects units

## 🐛 Bug Reports

When reporting bugs, include:

1. **Environment**: OS, Java version, MIDI setup
2. **Steps to reproduce**: Exact commands sent
3. **Expected vs actual**: What should happen vs what happens
4. **Logs**: Console output, error messages
5. **MIDI data**: If possible, MIDI monitor logs

## 💡 Feature Requests

For new features, explain:

1. **Use case**: What guitar workflow would this improve?
2. **AI interaction**: How would users describe this with natural language?
3. **MIDI requirements**: What MIDI functionality is needed?
4. **Examples**: Concrete examples of the feature in action

## 📄 Pull Request Process

1. **Ensure tests pass**: `mvn test` must succeed
2. **Update documentation**: README, CLAUDE.md if needed
3. **Clear description**: Explain what your PR does and why
4. **Link issues**: Reference any related issues
5. **Request review**: Tag maintainers for review

### PR Template
```markdown
## Changes
- Added support for [Pedal Name]
- Fixed [specific issue]
- Improved [functionality]

## Testing
- [ ] All existing tests pass
- [ ] Added tests for new functionality  
- [ ] Tested with real hardware (if applicable)

## Documentation
- [ ] Updated README if needed
- [ ] Added usage examples
- [ ] Documented any breaking changes
```

## 🤝 Community

- **Be respectful**: We're all here to make music better
- **Help others**: Answer questions, review PRs
- **Share knowledge**: Document your discoveries
- **Have fun**: This is about making music more creative!

## ❓ Questions?

- **General questions**: Open a GitHub Discussion
- **Bug reports**: Use the bug report template
- **Feature ideas**: Use the feature request template  
- **Pedal requests**: Use the pedal request template

---

**Thank you for contributing to the future of AI-controlled guitar effects!** 🎸🤖