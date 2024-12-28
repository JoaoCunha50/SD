#!/bin/bash
# ===============================
# PERFORMANCE TEST SCRIPT: PUT Operations
# ===============================
#
# This script is designed to test the performance of PUT operations on a Java application.
# It registers a user, inserts small, medium, and large values using PUT commands, 
# and measures the total execution time.
#
# The script generates random data of different sizes, performs PUT operations to 
# insert the data, and calculates the time it takes to insert all values.
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

# Credentials for login during the performance test
username="test_user_put"
password="test_password_put"

# Function to generate a random string of alphanumeric characters
# The length of the string is passed as an argument (default is 32 characters)
generate_random_string() {
    cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w ${1:-32} | head -n 1
}

# Create a temporary file to store the complete script
temp_script=$(mktemp)

# Visual header for the output
draw_line
echo -e "${YELLOW}${BOLD}    PERFORMANCE TEST FOR PUTS    ${NC}"
draw_line

# Start building the script with login commands
cat << EOF > "$temp_script"
register
$username
$password
EOF

echo -e "${CYAN}➤ Generating small values...${NC}"
# Add PUT commands for small values (10 characters each)
for i in {1..5}; do
    key="small_key_$i"
    value=$(generate_random_string 10)
    echo "put" >> "$temp_script"
    echo "$key" >> "$temp_script"
    echo "$value" >> "$temp_script"
done

echo -e "${CYAN}➤ Generating medium values...${NC}"
# Add PUT commands for medium values (100 characters each)
for i in {1..5}; do
    key="medium_key_$i"
    value=$(generate_random_string 100)
    echo "put" >> "$temp_script"
    echo "$key" >> "$temp_script"
    echo "$value" >> "$temp_script"
done

echo -e "${CYAN}➤ Generating large values...${NC}"
# Add PUT commands for large values (1000 characters each)
for i in {1..5}; do
    key="large_key_$i"
    value=$(generate_random_string 1000)
    echo "put" >> "$temp_script"
    echo "$key" >> "$temp_script"
    echo "$value" >> "$temp_script"
done

# Add the exit command to the script
echo "exit" >> "$temp_script"

echo -e "${PURPLE}Starting tests...${NC}"
draw_line

# Execute the entire script at once and measure the total execution time
start_time=$(date +%s.%N)   # Capture start time
cat "$temp_script" | $java_program
end_time=$(date +%s.%N)     # Capture end time
duration=$(echo "$end_time - $start_time" | bc)  # Calculate the duration

draw_line
# Display the total execution time for the PUT operations
echo -e "${BOLD}Total execution time:${NC} ${GREEN}$duration${NC} seconds"
draw_line

# Clean up the temporary file
rm "$temp_script"

