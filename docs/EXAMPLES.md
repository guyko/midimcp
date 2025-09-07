# 🎵 MIDI MCP Usage Examples

This document shows real-world examples of how to use the MIDI Guitar Pedal MCP Server with AI assistants.

## 🤖 AI Assistant Conversations

### Example 1: Making Delay Brighter

**User**: "Make my delay sound brighter and more present"

**AI Assistant** (internal processing):
- "brighter" → increase filter frequency (CC 5)
- "more present" → increase mix level (CC 1)

**AI sends to MCP**:
```json
{
  "tool": "execute_midi_commands",
  "arguments": {
    "pedalId": "meris_lvx",
    "commands": [
      {
        "ccNumber": 5,
        "value": 95,
        "description": "Increase filter for brightness"
      },
      {
        "ccNumber": 1, 
        "value": 65,
        "description": "Increase mix for presence"
      }
    ]
  }
}
```

**MCP Response**:
```
MIDI Commands Execution:
Pedal: Meris LVX
Commands executed: 2
Successful: 2
Failed: 0

Command 1:
  Status: SUCCESS
  Message: Mock MIDI executed: B0 05 5F

Command 2:
  Status: SUCCESS  
  Message: Mock MIDI executed: B0 01 41
```

**Result**: Your delay now has a brighter, more present character! 🌟

### Example 2: Vintage Slapback Delay

**User**: "Give me that classic 50s rockabilly slapback sound"

**AI Assistant** analyzes request:
- "classic 50s" → tape engine + short time
- "rockabilly slapback" → single repeat, quick decay
- "vintage" → add some drive/saturation

**AI sends to MCP**:
```json
{
  "tool": "execute_midi_commands",
  "arguments": {
    "pedalId": "meris_lvx", 
    "commands": [
      {"ccNumber": 0, "value": 1, "description": "Switch to Tape engine"},
      {"ccNumber": 3, "value": 30, "description": "Short delay time ~100ms"},
      {"ccNumber": 4, "value": 15, "description": "Low feedback for single repeat"},
      {"ccNumber": 1, "value": 25, "description": "Subtle mix level"},
      {"ccNumber": 13, "value": 40, "description": "Add tape saturation"},
      {"ccNumber": 5, "value": 35, "description": "Warm filter setting"}
    ]
  }
}
```

**Result**: Perfect rockabilly slapback with vintage tape character! 🎸

### Example 3: Ambient Soundscape  

**User**: "Create a huge ambient wash for atmospheric guitar"

**AI Assistant** interprets:
- "huge" → high mix, wide stereo, long tails
- "ambient" → diffusion, modulation, spaciousness
- "atmospheric" → enhance stereo field

```json
{
  "tool": "execute_midi_commands",
  "arguments": {
    "pedalId": "meris_lvx",
    "commands": [
      {"ccNumber": 1, "value": 85, "description": "High wet mix for ambient"},
      {"ccNumber": 4, "value": 70, "description": "Long feedback tails"},
      {"ccNumber": 11, "value": 75, "description": "High diffusion for space"},
      {"ccNumber": 8, "value": 45, "description": "Modulation depth for movement"},
      {"ccNumber": 7, "value": 25, "description": "Slow modulation rate"},
      {"ccNumber": 9, "value": 90, "description": "Wide stereo image"},
      {"ccNumber": 10, "value": 60, "description": "Ping pong for movement"}
    ]
  }
}
```

**Result**: Lush, expansive ambient textures perfect for soundscapes! 🌌

## 🎛️ Working with Different Pedals

### Adding Your Own Pedal

**User**: "Add my Strymon Timeline pedal"

```json
{
  "tool": "add_pedal",
  "arguments": {
    "manufacturer": "Strymon",
    "modelName": "Timeline",
    "version": "1.0",
    "midiChannel": 1,
    "parameters": [
      {
        "name": "Mix",
        "ccNumber": 1, 
        "minValue": 0,
        "maxValue": 127,
        "description": "Wet/dry mix balance",
        "unit": "%",
        "category": "Global"
      },
      {
        "name": "Time",
        "ccNumber": 2,
        "minValue": 0, 
        "maxValue": 127,
        "description": "Delay time",
        "unit": "ms",
        "category": "Delay"
      }
    ],
    "description": "Strymon Timeline professional delay pedal"
  }
}
```

### Querying Pedal Information

**User**: "What parameters can I control on my LVX?"

```json
{
  "tool": "get_pedal",
  "arguments": {
    "pedalId": "meris_lvx"
  }
}
```

**Response**: Complete parameter list with CC numbers, ranges, and descriptions.

## 🎯 Command Patterns

### Single Parameter Changes
```json
{
  "tool": "execute_midi_command",
  "arguments": {
    "pedalId": "meris_lvx",
    "ccNumber": 3,
    "value": 64,
    "description": "Set delay time to medium"
  }
}
```

### Preset-Style Changes (Multiple Parameters)
```json
{
  "tool": "execute_midi_commands", 
  "arguments": {
    "pedalId": "meris_lvx",
    "commands": [
      {"ccNumber": 0, "value": 2, "description": "Digital engine"},
      {"ccNumber": 1, "value": 50, "description": "50% mix"},
      {"ccNumber": 3, "value": 80, "description": "Long delay time"},
      {"ccNumber": 4, "value": 60, "description": "Moderate feedback"}
    ]
  }
}
```

## 🔍 Debugging and Monitoring

### Check MIDI Status
```json
{
  "tool": "get_midi_status",
  "arguments": {}
}
```

**Response**:
```
MIDI Executor Status:
Available: true
Status: Mock MIDI executor (5 commands executed)
```

### List Available Pedals
```json
{
  "tool": "list_pedals",
  "arguments": {}
}
```

**Response**:
```
Available pedals:
meris_lvx: Meris LVX (19 parameters)
strymon_timeline: Strymon Timeline (12 parameters)
```

## 🎸 Musical Workflow Examples

### Building a Guitar Part

1. **Start Clean**: "Set my delay to a subtle doubling effect"
   - Short time, low feedback, low mix

2. **Add Texture**: "Now make it more spacious for the verse"
   - Increase diffusion, add modulation

3. **Big Chorus**: "Huge delay wash for the chorus"
   - High mix, long tails, wide stereo

4. **Solo Section**: "Bright, cutting delay for the solo"
   - High filter, medium mix, tape saturation

### Sound Design Session

1. **Experiment**: "Give me something weird and experimental"
   - Reverse engine, high modulation, extreme settings

2. **Refine**: "Make it more musical but keep the weirdness"
   - Adjust mix, moderate extreme settings

3. **Performance**: "Lock in these settings for live use"
   - Document exact CC values for recall

## 📋 Common AI Phrases and Their Interpretations

| User Says | AI Typically Interprets As |
|-----------|----------------------------|
| "Brighter" | Increase filter frequency (CC 5) |
| "Warmer/Darker" | Decrease filter frequency (CC 5) |
| "More delay" | Increase time (CC 3) and/or feedback (CC 4) |
| "Subtle" | Decrease mix (CC 1) |
| "Ambient" | Increase mix, diffusion, modulation |
| "Slapback" | Short time, low feedback |
| "Vintage" | Tape engine, drive, warm filter |
| "Clean/Digital" | Digital engine, high filter, no drive |
| "Wide" | Increase stereo width, ping pong |
| "Movement" | Add modulation rate/depth |

## 🚨 Troubleshooting

### Command Not Working?
1. Check pedal ID: `list_pedals`
2. Verify CC number: `get_pedal` 
3. Check MIDI status: `get_midi_status`
4. Validate CC range (0-127)

### Pedal Not Responding?
1. Check MIDI cable connections
2. Verify pedal MIDI channel matches
3. Ensure pedal MIDI mode is enabled
4. Test with direct MIDI monitor

### AI Not Understanding?
1. Be more specific: "Increase delay time" vs "more delay"
2. Use pedal terminology: "feedback" vs "repeats"
3. Reference specific parameters: "adjust the filter"

---

**🎵 Happy music making with AI-controlled effects!** 🤖🎸