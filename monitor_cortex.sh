#!/bin/bash

echo "=== Quad Cortex Network Traffic Monitor ==="
echo "This script will monitor network traffic while you use Cortex Control"
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run with sudo for packet capture:"
    echo "sudo ./monitor_cortex.sh"
    exit 1
fi

# Create capture directory
mkdir -p cortex_captures
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
CAPTURE_FILE="cortex_captures/cortex_traffic_${TIMESTAMP}.pcap"

echo "Starting packet capture..."
echo "Capture file: $CAPTURE_FILE"
echo ""
echo "Instructions:"
echo "1. Now perform ONE action in Cortex Control (e.g., add 808 overdrive)"
echo "2. Press Ctrl+C when done to stop capture"
echo "3. We'll analyze the captured traffic"
echo ""
echo "Monitoring all traffic on local network (10.0.0.0/24)..."
echo "Press Ctrl+C to stop"
echo ""

# Start packet capture
tcpdump -i any -w "$CAPTURE_FILE" net 10.0.0.0/24

echo ""
echo "Capture stopped. Analyzing traffic..."
echo ""

# Basic analysis
echo "=== Traffic Summary ==="
tcpdump -r "$CAPTURE_FILE" -n | head -20

echo ""
echo "=== HTTP Traffic ==="
tcpdump -r "$CAPTURE_FILE" -A port 80 | head -20

echo ""
echo "=== HTTPS/SSL Traffic ==="
tcpdump -r "$CAPTURE_FILE" -n port 443 | head -10

echo ""
echo "=== Custom Ports ==="
tcpdump -r "$CAPTURE_FILE" -n not port 443 and not port 80 and not port 22 and not port 53 | head -10

echo ""
echo "Capture saved to: $CAPTURE_FILE"
echo "You can analyze further with: wireshark $CAPTURE_FILE"