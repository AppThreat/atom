def helper(x):
    return x + 1


def ping(n):
    if n <= 0:
        return 0
    return pong(n - 1)


def pong(n):
    return ping(n - 1)


def main():
    y = helper(5)
    ping(y)
    print(y)


main()
