import socket


def start_server():
    host = '127.0.0.1'
    port = 65432
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((host, port))
        print(f"Python: Очікування з'єднання на {host}:{port}...")
        s.listen()
        conn, addr = s.accept()
        with conn:
            print(f"Python: Підключено клієнта {addr}")
            while True:
                data = conn.recv(1024)
                if data:
                    number = data.decode('utf-8')
                    print(f"Python [LOG]: Отримано число від Java: {number}", end="")
                    conn.sendall(data)
                    print("Python: Число відправлено назад.")


if __name__ == "__main__":
    start_server()
