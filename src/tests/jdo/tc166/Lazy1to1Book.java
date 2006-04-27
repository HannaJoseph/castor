/*
 * Copyright 2005 Ralf Joachim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdo.tc166; 

public class Lazy1to1Book { 
	private Long id; 
	private String name; 
	private Lazy1to1Author author;
	
	public Long getId() { return id; } 
	public void setId(Long id) { this.id = id; } 

    public String getName() { return name; } 
    public void setName(String name) { this.name = name; } 

    public Lazy1to1Author getAuthor() { return author; } 
	public void setAuthor(Lazy1to1Author author) { this.author = author; } 
}
