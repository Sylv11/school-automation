package com.example.sylvain.projetautomates.DB;

// User class (user table)
public class User
{
    private int id;
    private String lastname;
    private String firstname;
    private String email;
    private String password;
    private int rank;

    public User(String l, String f, String e, String p, int r) {
        this.lastname = l;
        this.firstname = f;
        this.email = e;
        this.password = p;
        this.rank = r;
    }

    public void setId(int i){
        this.id = i;
    }

    public int getId(){
        return this.id;
    }

    public void setLastname(String l){
        this.lastname = l;
    }

    public String getLastname(){
        return this.lastname;
    }

    public void setFirstname(String f){
        this.firstname = f;
    }

    public String getFirstname(){
        return this.firstname;
    }

    public void setEmail(String e){
        this.email = e;
    }

    public String getEmail(){
        return this.email;
    }

    public void setPassword(String p){
        this.password = p;
    }

    public String getPassword(){
        return this.password;
    }

    public void setRank(int r){
        this.rank = r;
    }

    public int getRank(){
        return this.rank;
    }
}
