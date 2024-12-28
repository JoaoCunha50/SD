#!/bin/bash
# ===============================
# PERFORMANCE TEST SCRIPT: PUT Operations for Multiple Clients with Unique Keys
# ===============================
#
# This script tests the performance of PUT operations for 5 clients on a Java application.
# Each client is registered with unique usernames and each client gets unique keys for their PUT operations.
# The script measures and displays the time taken for each client’s operations separately.
# ===============================

# Definition of colors for terminal output
GREEN='\033[0;32m'      # Green for success messages
BLUE='\033[0;34m'       # Blue for general messages
YELLOW='\033[1;33m'     # Yellow for emphasis
CYAN='\033[0;36m'       # Cyan for info messages
PURPLE='\033[0;35m'     # Purple for special info
BOLD='\033[1m'          # Bold text
NC='\033[0m'            # No Color (reset)

# Function to draw a line in the terminal for visual separation
draw_line() {
    echo -e "${BLUE}=================================${NC}"
}

# Java program configuration (this assumes the Java application is in the ../bin directory)
java_program="java -cp ../bin client.ClientInterface"

# Function to generate a random string of alphanumeric characters
# The length of the string is passed as an argument (default is 32 characters)
generate_random_string() {
    cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w ${1:-32} | head -n 1
}

# Function to register a client and perform PUT operations
register_and_put() {
    local username=$1
    local password="test_password"

    # Create a temporary file to store the script for this client
    temp_script=$(mktemp)

    # Build the script to register the client and perform PUT operations
    cat << EOF > "$temp_script"
register
$username
$password
EOF

    echo -e "${CYAN}➤ Generating small values for $username...${NC}"
    # Add PUT commands for small values (10 characters each) with unique keys
    for i in {1..5}; do
        key="small_key_${username}_$i"
        value=$(generate_random_string 10)
        echo "put" >> "$temp_script"
        echo "$key" >> "$temp_script"
        echo "$value" >> "$temp_script"
    done

    echo -e "${CYAN}➤ Generating medium values for $username...${NC}"
    # Add PUT commands for medium values (100 characters each) with unique keys
    for i in {1..5}; do
        key="medium_key_${username}_$i"
        value=$(generate_random_string 100)
        echo "put" >> "$temp_script"
        echo "$key" >> "$temp_script"
        echo "$value" >> "$temp_script"
    done

    echo -e "${CYAN}➤ Generating large values for $username...${NC}"
    # Add PUT commands for large values (1000 characters each) with unique keys
    for i in {1..5}; do
        key="large_key_${username}_$i"
        value=$(generate_random_string 1000)
        echo "put" >> "$temp_script"
        echo "$key" >> "$temp_script"
        echo "$value" >> "$temp_script"
    done

    # Add the exit command to the script
    echo "exit" >> "$temp_script"

    echo -e "${PURPLE}Starting tests for $username...${NC}"
    draw_line

    # Measure the time for PUT operations for this client
    start_time=$(date +%s.%N)   # Capture start time
    cat "$temp_script" | $java_program
    end_time=$(date +%s.%N)     # Capture end time
    duration=$(echo "$end_time - $start_time" | bc)  # Calculate the duration

    draw_line
    # Display the total execution time for the PUT operations for this client
    echo -e "${BOLD}Total execution time for $username's PUT operations:${NC} ${GREEN}$duration${NC} seconds"
    draw_line

    # Clean up the temporary file
    rm "$temp_script"
}

# Register and perform PUT operations for 5 clients
for i in {1..20}; do
    username="client_user_$i"
    register_and_put $username
done
