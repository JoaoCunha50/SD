#!/bin/bash

# ===============================
# PERFORMANCE TEST SCRIPT: GET Operations
# ===============================
#
# This script is designed to test the performance of GET operations on a Java application.
# It registers a user, inserts small, medium, and large values using PUT commands, 
# and then performs GET operations to retrieve those values. The total execution time of 
# the GET operations is measured and displayed.
#
# The script generates random data of different sizes, executes the PUT and GET commands,
# and calculates the time it takes to perform all GET operations.
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
username="test_user_get"
password="test_password_get"

# Function to generate a random string of alphanumeric characters
# The length of the string is passed as an argument (default is 32 characters)
generate_random_string() {
    cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w ${1:-32} | head -n 1
}

# Create temporary files to store the setup and GET scripts
setup_script=$(mktemp)   # Temporary file for setup (insert data)
get_script=$(mktemp)     # Temporary file for GET commands

# Visual header for the output
draw_line
echo -e "${YELLOW}${BOLD}    PERFORMANCE TEST FOR GETS    ${NC}"
draw_line

# Setup script: registers the user and inserts values into the system
cat << EOF > "$setup_script"
register
$username
$password
EOF

echo -e "${CYAN}➤ Generating small values...${NC}"
# Insert small values (10 characters each) into the system
for i in {1..5}; do
    key="small_key_$i"
    value=$(generate_random_string 10)
    echo "put" >> "$setup_script"
    echo "$key" >> "$setup_script"
    echo "$value" >> "$setup_script"
    # Add corresponding GET commands to the GET script
    echo "get" >> "$get_script"
    echo "$key" >> "$get_script"
done

echo -e "${CYAN}➤ Generating medium values...${NC}"
# Insert medium values (100 characters each) into the system
for i in {1..5}; do
    key="medium_key_$i"
    value=$(generate_random_string 100)
    echo "put" >> "$setup_script"
    echo "$key" >> "$setup_script"
    echo "$value" >> "$setup_script"
    # Add corresponding GET commands to the GET script
    echo "get" >> "$get_script"
    echo "$key" >> "$get_script"
done

echo -e "${CYAN}➤ Generating large values...${NC}"
# Insert large values (1000 characters each) into the system
for i in {1..5}; do
    key="large_key_$i"
    value=$(generate_random_string 1000)
    echo "put" >> "$setup_script"
    echo "$key" >> "$setup_script"
    echo "$value" >> "$setup_script"
    # Add corresponding GET commands to the GET script
    echo "get" >> "$get_script"
    echo "$key" >> "$get_script"
done

# Add exit commands to both scripts (to close the session)
echo "exit" >> "$setup_script"
echo "exit" >> "$get_script"

# Prepare the login script, which will execute the GET commands after login
login_script=$(mktemp)
cat << EOF > "$login_script"
login
$username
$password
$(cat "$get_script")
EOF

echo -e "${PURPLE}Setting up environment (inserting values)...${NC}"
# Execute the setup script to register the user and insert the data into the system
cat "$setup_script" | $java_program

echo -e "${PURPLE}Starting GET tests...${NC}"
draw_line

# Measure the execution time for all GET commands
start_time=$(date +%s.%N)   # Capture start time
cat "$login_script" | $java_program
end_time=$(date +%s.%N)     # Capture end time
duration=$(echo "$end_time - $start_time" | bc)  # Calculate the duration

draw_line
# Display the total execution time for the GET operations
echo -e "${BOLD}Total execution time for GETs:${NC} ${GREEN}$duration${NC} seconds"
draw_line

# Clean up the temporary files
rm "$setup_script"
rm "$get_script"
rm "$login_script"

