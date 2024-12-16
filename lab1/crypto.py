#!/usr/bin/env python3 -tt
"""
File: crypto.py
---------------
Assignment 1: Cryptography
Course: CS 41
Name: <YOUR NAME>
SUNet: <SUNet ID>

Replace this with a description of the program.
"""
import utils

def offset_char(char, offset):
    return chr(ord(char) + offset)

def offset_uppercase(char, offset):
    return offset_char('A', (ord(offset_char(char, offset)) - ord('A')) % 26)

# Caesar Cipher

def encrypt_caesar(plaintext):
    """Encrypt plaintext using a Caesar cipher.
    """
    encrypted = []
    for letter in plaintext:
        if (not str.isalpha(letter)):
            encrypted += letter
        else:
            encrypted += offset_uppercase(letter, 3)
    return "".join(encrypted)


def decrypt_caesar(ciphertext):
    """Decrypt a ciphertext using a Caesar cipher.
    """
    decrypted = []
    for letter in ciphertext:
        if (not str.isalpha(letter)):
            decrypted += letter
        else:
            decrypted += offset_uppercase(letter, -3)
    return "".join(decrypted)


# Vigenere Cipher

def encrypt_vigenere(plaintext, keyword):
    """Encrypt plaintext using a Vigenere cipher with a keyword.
    """
    encrypted = []
    for i in range(len(plaintext)):
        letter = plaintext[i]
        keyword_letter = keyword[i % len(keyword)]
        if (not str.isalpha(letter)):
            encrypted += letter
        else:
            encrypted += offset_uppercase(letter, ord(keyword_letter) - ord('A'))
    return "".join(encrypted)


def decrypt_vigenere(ciphertext, keyword):
    """Decrypt ciphertext using a Vigenere cipher with a keyword.
    """
    decrypted = []
    for i in range(len(ciphertext)):
        letter = ciphertext[i]
        keyword_letter = keyword[i % len(keyword)]
        if (not str.isalpha(letter)):
            decrypted += letter
        else:
            decrypted += offset_uppercase(letter, -(ord(keyword_letter) - ord('A')))
    return "".join(decrypted)

# Scytale Cipher

def encrypt_scytale(plaintext: str, circumference: int):
    """Encrypt plaintext using a Scytale cipher with a given circumference.
    """
    encrypted = ""
    start = 0
    n = len(plaintext)
    while (start < circumference):
        encrypted += plaintext[start:n:circumference]
        start += 1
    return encrypted


def decrypt_scytale(ciphertext: str, circumference: int):
    """Decrypt ciphertext using a Scytale cipher with a given circumference.
    """
    decrypted = ""
    start = 0
    n = len(ciphertext)
    step = n // circumference if n % circumference == 0 else n // circumference + 1
    full_lines = (n - 1) % circumference + 1
    while (start < step):
        f = full_lines
        i = start
        while (f > 0):
            decrypted += ciphertext[i]
            i += step
            f -= 1
        if (len(decrypted) < n):
            decrypted += ciphertext[i:n:(step-1)]
        start += 1
    return decrypted

# Railfence Cipher

def encrypt_railfence(plaintext: str, circumference: int):
    """Encrypt ciphertext using a Railfence cipher with a given circumference.
    """
    cipher_lines = circumference * ['']
    i = 0
    j = 0
    n = len(plaintext)

    while i < n:
        for j in range(circumference):
            if i < n:
                cipher_lines[j] += plaintext[i]
                i += 1
            else:
                break

        for j in range(circumference - 2, 0, -1):
            if i < n:
                cipher_lines[j] += plaintext[i]
                i += 1
            else:
                break
    
    ciphertext = ''.join(cipher_lines)
    return ciphertext

def decrypt_railfence(ciphertext: str, circumference: int):
    """Decrypt ciphertext using a Railfence cipher with a given circumference.
    """
    decrypted = ""
    matrix = [['\0' for i in range(len(ciphertext))] for j in range(circumference)]
    ind = [0, 0]
    dir = 1
    for it in range(len(ciphertext)):
        matrix[ind[0]][ind[1]] = '.'

        ind[0] += dir
        ind[1] += 1
        if (ind[0] == circumference - 1):
            dir = -1
        elif (ind[0] == 0):
            dir = 1
    
    ind = 0
    for i in range(circumference):
        for j in range(len(ciphertext)):
            if (matrix[i][j] == '.'):
                matrix[i][j] = ciphertext[ind]
                ind += 1

    ind = [0, 0]
    dir = 1
    for it in range(len(ciphertext)):
        decrypted += matrix[ind[0]][ind[1]]

        ind[0] += dir
        ind[1] += 1
        if (ind[0] == circumference - 1):
            dir = -1
        elif (ind[0] == 0):
            dir = 1
    return decrypted

def generate_private_key(n=8):
    raise NotImplementedError  # Your implementation here

def create_public_key(private_key):
    raise NotImplementedError  # Your implementation here

def encrypt_mh(message, public_key):
    raise NotImplementedError  # Your implementation here

def decrypt_mh(message, private_key):
    raise NotImplementedError  # Your implementation here