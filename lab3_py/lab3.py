import multiprocessing
import random


def logger_process(queue_in, queue_out):
    number = queue_in.get()  # Отримання
    print(f"Process: Логування числа {number}")
    queue_out.put(number)  # Повернення


if __name__ == "__main__":
    q_to_worker = multiprocessing.Queue()
    q_to_main = multiprocessing.Queue()

    p = multiprocessing.Process(target=logger_process, args=(q_to_worker, q_to_main))
    p.start()

    num = random.randint(1, 100)
    print(f"Main: Передача {num}")
    q_to_worker.put(num)

    result = q_to_main.get()
    print(f"Main: Отримано назад {result}")
    p.join()