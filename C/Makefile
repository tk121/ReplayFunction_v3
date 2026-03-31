CC=gcc
CFLAGS=-O2 -Wall -Wextra -pthread
INCLUDES=-I./common

COMMON_SRC=common/tcp_ipc_server.c

SERVICE_A_SRC=services/service_a/main.c services/service_a/handler.c
SERVICE_B_SRC=services/service_b/main.c services/service_b/handler.c

all: service_a service_b

service_a: $(COMMON_SRC) $(SERVICE_A_SRC)
	$(CC) $(CFLAGS) $(INCLUDES) -o $@ $^

service_b: $(COMMON_SRC) $(SERVICE_B_SRC)
	$(CC) $(CFLAGS) $(INCLUDES) -o $@ $^

clean:
	rm -f service_a service_b