WeChat: cstutorcs
QQ: 749389476
Email: tutorcs@163.com
g: int = 1
def foo(x: int) -> int:
    y: int = 2
    def bar() -> int:
        z: int = 3
        def baz() -> int:
            return y
        return baz()
    return bar()
    
print(foo(g))
