#!/bin/bash
# ===============================
# PERFORMANCE TEST SCRIPT: PUT Operations in Parallel for Multiple Clients
# ===============================

# Definition of colors for terminal output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m'

# Create results directory if it doesn't exist
results_dir="results"
mkdir -p "$results_dir"

# Draw a line in the terminal for visual separation
draw_line() {
    echo -e "${BLUE}=================================${NC}"
}

# Create temporary files to store results
results_file=$(mktemp)
json_file="$results_dir/multi_users_put_results.json"

# Java program configuration
java_program="java -cp ../../bin client.ClientInterface"

# Function to generate a random string of alphanumeric characters
generate_random_string() {
    cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w ${1:-32} | head -n 1
}

# Function to register a client and perform PUT operations
register_and_put() {
    local username=$1
    local password="test_password"
    temp_script=$(mktemp)

    # Build the script to register the client and perform PUT operations
    cat <<EOF >"$temp_script"
register
$username
$password
EOF

    for i in {1..5}; do
        key="small_key_${username}_$i"
        value=$(generate_random_string 10)
        echo "put" >>"$temp_script"
        echo "$key" >>"$temp_script"
        echo "$value" >>"$temp_script"
    done

    for i in {1..5}; do
        key="medium_key_${username}_$i"
        value=$(generate_random_string 100)
        echo "put" >>"$temp_script"
        echo "$key" >>"$temp_script"
        echo "$value" >>"$temp_script"
    done

    for i in {1..5}; do
        key="large_key_${username}_$i"
        value=$(generate_random_string 1000)
        echo "put" >>"$temp_script"
        echo "$key" >>"$temp_script"
        echo "$value" >>"$temp_script"
    done

    echo "exit" >>"$temp_script"

    # Measure the time for PUT operations for this client
    start_time=$(date +%s.%N)
    cat "$temp_script" | $java_program >/dev/null 2>&1
    end_time=$(date +%s.%N)
    duration=$(echo "$end_time - $start_time" | bc)

    # Extract client number from username for sorting
    client_num=$(echo "$username" | grep -o '[0-9]\+')
    # Save result to temporary file with client number, username and duration
    printf "%d %s %s\n" "$client_num" "$username" "$duration" >>"$results_file"

    rm "$temp_script"
}

max_parallel=15
pids=()

# Initialize JSON file with opening brace
echo "{" >"$json_file"

for i in {1..15}; do
    username="client_user_$i"

    # Run in background
    register_and_put $username &
    pids+=($!) # Store the process ID (PID)

    # Wait for background jobs if we reach the max_parallel limit
    if ((${#pids[@]} >= max_parallel)); then
        wait -n               # Wait for any background job to finish
        pids=(${pids[@]/$!/}) # Remove completed PID
    fi
done

# Wait for any remaining background jobs
wait

# Print sorted results and build JSON
echo -e "\n${BOLD}Execution times sorted by client number:${NC}"
draw_line

# Process results and create JSON entries
sort -n "$results_file" | while read -r client_num username duration; do
    printf "${BOLD}%-20s${NC} ${GREEN}%s${NC} seconds\n" "$username" "$duration"
    # Add JSON entry (with comma for all except last line)
    if [ "$client_num" -eq 15 ]; then
        echo "  \"$username\": $duration" >>"$json_file"
    else
        echo "  \"$username\": $duration," >>"$json_file"
    fi
done

# Close JSON file with closing brace
echo "}" >>"$json_file"

draw_line

# Clean up temporary file
rm "$results_file"

echo -e "${GREEN}All operations completed!${NC}"
echo -e "${CYAN}Results also saved to $json_file${NC}"
