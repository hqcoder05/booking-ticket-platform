import time
import asyncio
import aiohttp
import sys

# Suppress asyncio 'Event loop is closed' warning on Windows
if sys.platform == 'win32':
    asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

url = 'http://localhost:8080/api/customer/concerts/123e4567-e89b-12d3-a456-426614174000/seats'
TOTAL_REQUESTS = 50000
CONCURRENCY = 1000

async def fetch(session):
    try:
        async with session.get(url) as response:
            return response.status
    except Exception:
        return 0

async def bound_fetch(sem, session):
    async with sem:
        return await fetch(session)

async def main():
    print(f'Starting STRESS TEST: {TOTAL_REQUESTS} requests with concurrency {CONCURRENCY}...')
    sem = asyncio.Semaphore(CONCURRENCY)
    connector = aiohttp.TCPConnector(limit=CONCURRENCY)
    
    start_time = time.time()
    
    async with aiohttp.ClientSession(connector=connector) as session:
        tasks = [asyncio.ensure_future(bound_fetch(sem, session)) for _ in range(TOTAL_REQUESTS)]
        results = await asyncio.gather(*tasks)
        
    end_time = time.time()
    
    success_count = results.count(200) + results.count(404)
    failed_count = TOTAL_REQUESTS - success_count
    
    duration = end_time - start_time
    rps = TOTAL_REQUESTS / duration
    
    print('-----------------------------------------')
    print(f'Total Requests: {TOTAL_REQUESTS}')
    print(f'Concurrency Level: {CONCURRENCY}')
    print(f'Time taken: {duration:.2f} seconds')
    print(f'Requests per second (RPS): {rps:.2f}')
    print(f'Successful hits (200/404): {success_count}')
    print(f'Failed/Dropped requests: {failed_count}')
    print('-----------------------------------------')

if __name__ == '__main__':
    asyncio.run(main())
