WeChat: cstutorcs
QQ: 749389476
Email: tutorcs@163.com
class A(object):
    a:int = 42

class B(A):
    b:bool = True

    def __init__(self:"B"):
        print("B")

a:A = None
b:B = None

a = b = B()
print(a.a)
print(b.a)
print(b.b)
