# 🎸 MIDI Guitar Pedal MCP Server

[![Maven Build](https://img.shields.io/badge/maven-build%20passing-brightgreen)](https://maven.apache.org/)
[![Kotlin](https://img.shields.io/badge/kotlin-1.3.0-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![MCP Protocol](https://img.shields.io/badge/MCP-2024--11--05-purple)](https://modelcontextprotocol.io/)

A powerful **Model Context Protocol (MCP) server** that bridges AI assistants with MIDI guitar pedals. Control your guitar effects using natural language through any MCP-compatible AI assistant!

## 🚀 What is this?

**MIDI MCP** lets you control guitar pedals using natural language:

1. **You say**: *"Make my delay sound brighter"*  
2. **AI Assistant**: Interprets and generates `MidiCommand(channel=1, cc=5, value=100)`
3. **MCP Server**: Executes the command → Sends `B0 05 64` to your pedal
4. **Your Pedal**: Filter frequency increases, delay sounds brighter! 🎵

## ✨ Features

- 🎛️ **Persistent Pedal Knowledge** - Store MIDI CC mappings for any guitar pedal
- 🤖 **AI Assistant Integration** - Works with Claude, ChatGPT, or any MCP-compatible AI
- 🎵 **Real MIDI Output** - Sends actual MIDI commands to hardware
- 📦 **Pre-loaded Pedals** - Includes complete Meris LVX delay and Mercury X reverb pedals with full MIDI CC tables
- 🧪 **Fully Tested** - 25 focused tests covering real functionality
- ⚡ **Clean Architecture** - Separation between AI intelligence and MIDI execution

## 🏗️ Architecture

```
┌─────────────┐    Natural Language    ┌─────────────┐    MIDI Commands    ┌─────────────┐
│             │ ────────────────────── │             │ ─────────────────── │             │
│ AI Assistant│  "make it brighter"    │ MCP Server  │  B0 05 64 (bytes)   │ Guitar Pedal│
│             │ ◄────────────────────── │             │ ◄─────────────────── │             │
└─────────────┘    Execution Result    └─────────────┘    MIDI Response    └─────────────┘
```

**AI Assistant** handles the intelligence, **MCP Server** handles the execution.

## 🚀 Quick Start

### Prerequisites
- Java 8+ 
- Maven 3.6+
- MIDI interface (for real pedals) or use mock mode for testing

### 1. Clone and Build
```bash
git clone https://github.com/guyko/midimcp.git
cd midimcp
mvn compile
```

### 2. Run the MCP Server
```bash
mvn exec:java
# or
./run-server.sh
```

### 3. Connect Your AI Assistant
Configure your AI assistant to connect to the MCP server. The server provides these tools:
- `execute_midi_command` - Send single MIDI CC command
- `execute_midi_commands` - Send multiple commands in sequence  
- `get_pedal` - Get pedal information and parameters
- `list_pedals` - Show available pedals
- `add_pedal` - Add new pedal configurations

## 🎵 Example Usage

### Adding a New Pedal
```json
{
  "tool": "add_pedal",
  "arguments": {
    "manufacturer": "Strymon",
    "modelName": "Timeline", 
    "parameters": [
      {"name": "Mix", "ccNumber": 1, "description": "Wet/dry mix"},
      {"name": "Time", "ccNumber": 2, "description": "Delay time"}
    ]
  }
}
```

### Executing MIDI Commands
The AI assistant converts *"make it brighter"* into:
```json
{
  "tool": "execute_midi_command",
  "arguments": {
    "pedalId": "meris_lvx",
    "ccNumber": 5,
    "value": 100,
    "description": "Increase filter frequency for brightness"
  }
}
```

Result: `B0 05 64` sent to pedal → Filter opens up → Brighter delay sound! ✨

### Batch Commands for Complex Sounds
AI assistant interprets *"give me a vintage slapback"* and sends:
```json
{
  "tool": "execute_midi_commands", 
  "arguments": {
    "pedalId": "meris_lvx",
    "commands": [
      {"ccNumber": 0, "value": 1, "description": "Switch to Tape engine"},
      {"ccNumber": 3, "value": 35, "description": "Short delay time"},
      {"ccNumber": 4, "value": 20, "description": "Low feedback"},
      {"ccNumber": 1, "value": 30, "description": "Balanced mix"}
    ]
  }
}
```

### Mercury X Reverb Control
AI assistant interprets *"create a lush cathedral reverb"* and sends:
```json
{
  "tool": "execute_midi_commands", 
  "arguments": {
    "pedalId": "meris_mercury_x",
    "commands": [
      {"ccNumber": 32, "value": 25, "description": "Select Cathedra reverb structure"},
      {"ccNumber": 33, "value": 100, "description": "Long decay time"},
      {"ccNumber": 1, "value": 75, "description": "High wet mix"},
      {"ccNumber": 15, "value": 40, "description": "Medium predelay time"}
    ]
  }
}
```

## 🎛️ Included Pedals

### Meris LVX Delay (Pre-loaded)
Complete MIDI CC table including:
- **Engine Selection** (Vintage, Tape, Digital, Sweep, Reverse, Pitch)
- **Global Controls** (Mix, Level, Time, Feedback)  
- **Filtering** (Filter, Low Cut)
- **Modulation** (Rate, Depth)
- **Stereo** (Width, Ping Pong)
- **Advanced** (Diffusion, Smear, Drive)

### Meris Mercury X Reverb (Pre-loaded)
Complete MIDI CC table including:
- **8 Reverb Structures** (Ultraplate, Cathedra, Spring, 78 Room/Plate/Hall, Prism, Gravity)
- **Predelay System** (Time, Feedback, Cross Feedback, Modulation, Damping, Dry Blend)
- **Processing Elements** (Dynamics, Preamp, Filter, Pitch, Modulation)
- **Flexible Routing** (Pre+Dry, Pre, Feedback, Pre Tank, Post locations)
- **Expression Control** (6 assignable expression pedal mappings)
- **Advanced Features** (Gate controls, Hold Modifier, Tuner, MIDI Clock sync)

## 🧪 Testing

Run the comprehensive test suite:
```bash
mvn test
```

**25 focused tests** covering:
- ✅ MIDI byte generation and validation
- ✅ Command execution with mock hardware
- ✅ End-to-end AI assistant → MCP server → pedal workflows
- ✅ Error handling and failure scenarios
- ✅ Batch command coordination

### Test Examples
```kotlin
// AI generates command → MCP executes → Verifies MIDI bytes
testAIAssistantRequestsBrighterSound()
// Expected: B0 05 64 sent to hardware

// Multiple commands for complex sounds
testAIAssistantRequestsSlapbackDelay() 
// Expected: B0 03 23, B0 04 14, B0 01 1E sequence
```

## 🔧 Development

### Project Structure
```
src/main/java/com/guyko/
├── models/          # Data models (PedalModel, MidiCommand, CCParameter)
├── midi/            # MIDI execution layer (executors, results)
├── persistence/     # JSON-based pedal storage
├── mcp/             # MCP server implementation  
└── pedals/          # Pre-configured pedal definitions
```

### Adding New Pedals
1. Create parameter definitions with CC mappings
2. Use `PedalRepository` to save configurations
3. Test with `MockMidiExecutor` before hardware

### Architecture Principles
- **AI Assistant**: Natural language → MIDI commands (external)
- **MCP Server**: Command validation → Hardware execution (this project)
- **Clean separation**: No NLP in MCP server, no hardware details in AI

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-pedal`)
3. Add your pedal configurations or improvements
4. Write tests for new functionality
5. Commit changes (`git commit -m 'Add Amazing Pedal support'`)
6. Push to branch (`git push origin feature/amazing-pedal`)
7. Open a Pull Request

### Development Setup
```bash
# Clone and build
git clone https://github.com/guyko/midimcp.git
cd midimcp
mvn compile

# Run tests
mvn test

# Start development server
mvn exec:java
```

## 📋 Roadmap

- [ ] **More Pedals** - Add popular delay, reverb, overdrive pedals
- [ ] **MIDI Learn** - Auto-discover CC mappings from pedals
- [ ] **Preset Management** - Save/recall complete pedal settings
- [ ] **Real-time Monitoring** - Live MIDI traffic visualization  
- [ ] **Plugin Support** - VST/AU plugin control via MIDI
- [ ] **Web Interface** - Browser-based pedal control dashboard

## ❓ FAQ

**Q: Does this work with any MIDI pedal?**  
A: Yes! Add your pedal's CC mappings using the `add_pedal` tool.

**Q: Which AI assistants are supported?**  
A: Any MCP-compatible assistant (Claude, ChatGPT with MCP, etc.)

**Q: Can I use this without real MIDI hardware?**  
A: Yes! Use `MockMidiExecutor` for testing and development.

**Q: How do I add my pedal?**  
A: Check the Meris LVX example in `MerisLVXLoader.kt` and create similar mappings.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Model Context Protocol](https://modelcontextprotocol.io/) for the excellent standard
- [Meris](https://www.meris.us/) for detailed MIDI documentation
- Guitar effects community for inspiration

---

<div align="center">

**🎸 Rock on with AI-controlled guitar effects! 🤖**

[⭐ Star this repo](https://github.com/guyko/midimcp) • [🐛 Report Bug](https://github.com/guyko/midimcp/issues) • [💡 Request Feature](https://github.com/guyko/midimcp/issues)

</div>