import time
import requests
import concurrent.futures

url = 'http://localhost:8080/api/customer/concerts/123e4567-e89b-12d3-a456-426614174001/seats'
TOTAL_REQUESTS = 10000
CONCURRENCY = 100

def fetch():
    try:
        response = requests.get(url)
        return response.status_code
    except Exception as e:
        return 0

print(f'Starting load test: {TOTAL_REQUESTS} requests with concurrency {CONCURRENCY}...')
start_time = time.time()

success_count = 0
with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
    results = list(executor.map(lambda _: fetch(), range(TOTAL_REQUESTS)))

end_time = time.time()

success_count = results.count(200)
empty_or_404_count = results.count(404) + results.count(200) # In case it returns 200 []

duration = end_time - start_time
rps = TOTAL_REQUESTS / duration

print('-----------------------------------------')
print(f'Total Requests: {TOTAL_REQUESTS}')
print(f'Concurrency Level: {CONCURRENCY}')
print(f'Time taken: {duration:.2f} seconds')
print(f'Requests per second (RPS): {rps:.2f}')
print(f'Successful hits: {len([r for r in results if r in (200, 404)])}')
print('-----------------------------------------')
