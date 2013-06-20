<#assign licenseFirst = "/*">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "../Licenses/${project.license}">
/*
* Prolog Source File ${name}
*   created ${date} at ${time}
*   created by ${user}
*/

% fibonacci(0) = 0
% fibonacci(1) = 1
% fibonacci(2) = 1
% fibonacci(n) = fibonacci(n-1) + fibonacci(n-2)

fib(0,0).
fib(1,1).
fib(2,1).
fib(N,F) :-
    N > 2,
    N1 is N - 1,
    N2 is N - 2,
    fib(N1,F1),
    fib(N2,F2),
    F is F1 + F2.
