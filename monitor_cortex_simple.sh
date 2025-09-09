#!/bin/bash

echo "=== Quad Cortex Network Traffic Monitor (Simple) ==="
echo "Monitoring network connections for Cortex Control..."
echo ""

# Find Cortex Control process
CORTEX_PID=$(ps aux | grep "Cortex Control" | grep -v grep | awk '{print $2}')

if [ -z "$CORTEX_PID" ]; then
    echo "Cortex Control not found running!"
    exit 1
fi

echo "Found Cortex Control running (PID: $CORTEX_PID)"
echo ""

echo "=== Current Network Connections ==="
echo "Checking what Cortex Control is connected to..."

# Monitor network connections
lsof -i -P -p $CORTEX_PID 2>/dev/null

echo ""
echo "=== Monitoring for new connections ==="
echo "Now perform an action in Cortex Control (add 808 overdrive)..."
echo "Press Ctrl+C to stop monitoring"
echo ""

# Monitor changes in network connections
while true; do
    CONNECTIONS=$(lsof -i -P -p $CORTEX_PID 2>/dev/null)
    if [ ! -z "$CONNECTIONS" ]; then
        echo "$(date): Active connections detected:"
        echo "$CONNECTIONS"
        echo "---"
    fi
    sleep 2
done