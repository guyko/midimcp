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
- 📦 **Pre-loaded Pedals** - Includes Meris LVX delay, Mercury X reverb, Enzo X synthesizer, and Neural DSP Quad Cortex with full MIDI CC tables
- 🎛️ **Preset Creation** - Create complete presets via multiple MIDI CC commands sent in sequence from natural language descriptions
- 🧪 **Fully Tested** - 25 focused tests covering real functionality
- ⚡ **Clean Architecture** - Separation between AI intelligence and MIDI execution

## 🏗️ Architecture

```
┌─────────────┐    Pedal Config Query  ┌─────────────┐    MIDI Commands    ┌─────────────┐
│             │ ────────────────────── │             │ ─────────────────── │             │
│ AI Assistant│  get_pedal("lvx")     │ MCP Server  │  B0 05 64 (bytes)   │ Guitar Pedal│
│             │ ◄────────────────────── │             │ ◄─────────────────── │             │
└─────────────┘    CC Mappings         └─────────────┘    MIDI Response    └─────────────┘
     │                                                                              ▲
     └─ "make it brighter" → CC5=100 ──────────────────────────────────────────────┘
```

**MCP Server** exposes pedal configurations and executes MIDI. **AI Assistant** translates natural language to MIDI using the exposed knowledge.

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

#### Claude Desktop Integration

**Step 1: Build the project**
```bash
cd /path/to/midimcp
mvn compile
```

**Step 2: Configure Claude Desktop**
Add this configuration to your Claude Desktop config file:

**Location**: `~/Library/Application Support/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "midi-guitar-pedals": {
      "command": "/path/to/your/midimcp/start-mcp-wrapper.sh",
      "args": []
    }
  }
}
```

**Replace `/path/to/your/midimcp/` with your actual project directory path.**

**Step 3: Restart Claude Desktop**
Completely quit and restart Claude Desktop for the configuration to take effect.

**Step 4: Test the connection**
After restarting, you should see the "midi-guitar-pedals" server available. You can test it by asking Claude to:
- "List available guitar pedals"
- "Show me the parameters for the LVX delay"
- "Execute a MIDI command to make the delay brighter"

#### Other MCP-Compatible AI Assistants
Configure your AI assistant to connect to the MCP server using the appropriate MCP client for your platform. The server provides these tools:
- `execute_midi_command` - Send single MIDI CC command
- `execute_midi_commands` - Send multiple commands in sequence  
- `get_pedal` - Get pedal information and parameters
- `list_pedals` - Show available pedals
- `add_pedal` - Add new pedal configurations
- `send_sysex` - Send sysex data directly to MIDI devices for preset uploads or custom messages

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

### Enzo X Synthesizer Control
AI assistant interprets *"create a warm polysynth pad"* and sends:
```json
{
  "tool": "execute_midi_commands", 
  "arguments": {
    "pedalId": "meris_enzo_x",
    "commands": [
      {"ccNumber": 22, "value": 40, "description": "Switch to Poly Synth mode"},
      {"ccNumber": 24, "value": 64, "description": "OSC1 triangle wave"},
      {"ccNumber": 39, "value": 80, "description": "Filter frequency for warmth"},
      {"ccNumber": 55, "value": 90, "description": "Slow amplitude attack"}
    ]
  }
}
```

### Quad Cortex Scene Control
AI assistant interprets *"switch to clean scene and enable reverb"* and sends:
```json
{
  "tool": "execute_midi_commands", 
  "arguments": {
    "pedalId": "neural_dsp_quad_cortex",
    "commands": [
      {"ccNumber": 43, "value": 1, "description": "Switch to Scene B"},
      {"ccNumber": 36, "value": 127, "description": "Enable footswitch B (reverb)"},
      {"ccNumber": 47, "value": 1, "description": "Switch to Scene mode"}
    ]
  }
}
```

### Preset Creation via CC Commands

Create complete pedal presets by sending multiple MIDI CC commands in sequence. The AI assistant interprets natural language descriptions and creates presets by sending targeted CC command sequences that configure all aspects of the pedal's sound.

#### How Preset Creation Works
1. **AI queries pedal capabilities** via `get_pedal` to understand available CC parameters
2. **AI interprets natural language** like "warm vintage slapback delay" 
3. **AI sends multiple CC commands** via `execute_midi_commands` to configure the preset
4. **User hears changes in real-time** as each CC command adjusts pedal parameters
5. **User saves preset on pedal** when satisfied with the sound

#### LVX Delay Preset Creation
AI assistant interprets *"create a warm vintage slapback delay"* and sends:
```json
{
  "tool": "execute_midi_commands",
  "arguments": {
    "pedalId": "meris_lvx",
    "commands": [
      {"ccNumber": 16, "value": 86, "description": "Switch to Tape delay engine"},
      {"ccNumber": 1, "value": 85, "description": "High wet mix signal"},
      {"ccNumber": 15, "value": 35, "description": "Short slapback timing"},
      {"ccNumber": 19, "value": 25, "description": "Low feedback for single repeat"},
      {"ccNumber": 21, "value": 15, "description": "Subtle tape warble modulation"},
      {"ccNumber": 80, "value": 75, "description": "Warm/dark filter tone"},
      {"ccNumber": 62, "value": 16, "description": "Light dynamics compression"}
    ]
  }
}
```

**Result**: Pedal configured in real-time! User can tweak and save the preset on the pedal itself. 🎛️

#### Mercury X Reverb Preset Creation
AI assistant interprets *"create a large cathedral reverb with warm predelay"* and sends:
```json
{
  "tool": "execute_midi_commands",
  "arguments": {
    "pedalId": "meris_mercury_x",
    "commands": [
      {"ccNumber": 5, "value": 1, "description": "Select Cathedra reverb structure"},
      {"ccNumber": 1, "value": 90, "description": "High wet mix signal"},
      {"ccNumber": 11, "value": 95, "description": "Very long decay time"},
      {"ccNumber": 12, "value": 45, "description": "Medium room size predelay"},
      {"ccNumber": 13, "value": 40, "description": "Noticeable predelay separation"},
      {"ccNumber": 25, "value": 2, "description": "Warm tube preamp type"},
      {"ccNumber": 31, "value": 1, "description": "High cut filter for warmth"},
      {"ccNumber": 33, "value": 85, "description": "Remove harsh frequencies"}
    ]
  }
}
```

#### Enzo X Synthesizer Preset Creation
AI assistant interprets *"create a classic poly synth pad with slow attack"* and sends:
```json
{
  "tool": "execute_midi_commands",
  "arguments": {
    "pedalId": "meris_enzo_x",
    "commands": [
      {"ccNumber": 5, "value": 1, "description": "Select Poly Synth mode"},
      {"ccNumber": 1, "value": 75, "description": "Balanced mix level"},
      {"ccNumber": 11, "value": 1, "description": "Sawtooth oscillator 1"},
      {"ccNumber": 17, "value": 2, "description": "Triangle oscillator 2"},
      {"ccNumber": 24, "value": 70, "description": "Medium filter brightness"},
      {"ccNumber": 25, "value": 20, "description": "Subtle filter resonance"},
      {"ccNumber": 29, "value": 80, "description": "Slow amplitude attack"},
      {"ccNumber": 31, "value": 85, "description": "High sustain level"},
      {"ccNumber": 32, "value": 60, "description": "Medium release time"}
    ]
  }
}
```

**Benefits**: Real-time preset creation with immediate audio feedback! Users hear every parameter change and can fine-tune before saving. 🎵

## 🎛️ Included Pedals

### Meris LVX Delay (Pre-loaded - MIDI Channel 2)
Complete MIDI CC table including:
- **Engine Selection** (Vintage, Tape, Digital, Sweep, Reverse, Pitch)
- **Global Controls** (Mix, Level, Time, Feedback)  
- **Filtering** (Filter, Low Cut)
- **Modulation** (Rate, Depth)
- **Stereo** (Width, Ping Pong)
- **Advanced** (Diffusion, Smear, Drive)

### Meris Mercury X Reverb (Pre-loaded - MIDI Channel 1)
Complete MIDI CC table including:
- **8 Reverb Structures** (Ultraplate, Cathedra, Spring, 78 Room/Plate/Hall, Prism, Gravity)
- **Predelay System** (Time, Feedback, Cross Feedback, Modulation, Damping, Dry Blend)
- **Processing Elements** (Dynamics, Preamp, Filter, Pitch, Modulation)
- **Flexible Routing** (Pre+Dry, Pre, Feedback, Pre Tank, Post locations)
- **Expression Control** (6 assignable expression pedal mappings)
- **Advanced Features** (Gate controls, Hold Modifier, Tuner, MIDI Clock sync)

### Meris Enzo X Synthesizer (Pre-loaded - MIDI Channel 3)
Complete MIDI CC table including:
- **5 Synth Modes** (Mono Synth, Poly Synth, Arp Synth, Dry Mono, Dry Poly)
- **Dual Oscillators** (Independent wave shapes, pitch offset, detune, cross modulation)
- **Advanced Filters** (Ladder, State Variable, Twin with noise injection)
- **ADSR Envelopes** (Filter and Amplitude envelopes with full ADSR control)
- **Processing Elements** (Drive, Ambience, Modulation with flexible routing)
- **Modulation Effects** (Chorus, Flanger, Vibrato, Phaser, Ring Mod)
- **MIDI Note Control** (Full polyphonic MIDI keyboard support, aftertouch)

### Neural DSP Quad Cortex (Pre-loaded - MIDI Channel 4)
Complete MIDI CC table including:
- **6 DSP Cores** (Most powerful floor modeler, 4-instrument processing)
- **Neural Capture Technology** (Capture real amps and effects)
- **Multiple Modes** (Preset, Scene, Stomp, Hybrid modes)
- **8 Footswitches** (A-H with individual enable/bypass control)
- **Advanced Looper X** (Up to 4+ minutes, punch in/out, reverse, quantize)
- **Expression Control** (2 expression pedals with MIDI CC assignment)
- **Program Management** (Scene selection, tempo control, tuner, gig view)

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